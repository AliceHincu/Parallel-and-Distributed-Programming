using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Lab4
{
    public class CallbacksHttpExecutor
    {
        private readonly List<string> _hosts;

        public CallbacksHttpExecutor(List<string> hosts)
        {
            _hosts = hosts;
        }

        public void Execute()
        {
            for (var i = 0; i < this._hosts.Count; i++)
            {
                Fetch(_hosts[i], i);
            }
        }

        private static void Fetch(string host, int id)
        {
            // -- make a request to the ip of the host

            // get ip of host
            var ipHostInfo = Dns.GetHostEntry(host.Split('/')[0]);
            var ipAddress = ipHostInfo.AddressList[0];
            var remoteEndpoint = new IPEndPoint(ipAddress, HttpParser.Port);

            // Stream socket create a stream socket, which enables reliable, two-way, connection-based byte streams without the duplication of data
            // and without preservation of boundaries.
            var clientSocket = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            // define the request parameters
            var request = new RequestWrapper
            {
                Socket = clientSocket,
                Hostname = host.Split('/')[0],
                Endpoint = host.Contains("/") ? host.Substring(host.IndexOf("/", StringComparison.Ordinal)) : "/", // '/~rlupsa/edu/pdp/progs/srv-begin-end.cs'
                RemoteEndPoint = remoteEndpoint,
                Id = id
            };

            // start connecting to the server
            request.Socket.BeginConnect(request.RemoteEndPoint, ConnectedCallback, request);
            Thread.Sleep(2000); // wait 2 seconds so that the main thread doesn't stop before it conencted.
        }

        private static void ConnectedCallback(IAsyncResult ar)
        {
            // when it finished connecting, this method is called

            var requestWrapper = (RequestWrapper) ar.AsyncState;
            if (requestWrapper != null)
            {
                var clientSocket = requestWrapper.Socket;
                var clientId = requestWrapper.Id;
                var hostname = requestWrapper.Hostname;

                // signal that we finished connecting
                clientSocket.EndConnect(ar);
                Console.WriteLine("{0} --> Socket connected to {1} ({2})", clientId, hostname, clientSocket.RemoteEndPoint);
            }

            if (requestWrapper == null) return;

            // create a request
            var byteData = Encoding.ASCII.GetBytes(HttpParser.GetRequestString(
                requestWrapper.Hostname,
                requestWrapper.Endpoint
            ));

            // start sending the request
            requestWrapper.Socket.BeginSend(byteData, 0, byteData.Length, 0, SentCallback, requestWrapper);
        }

        private static void SentCallback(IAsyncResult ar)
        {
            // this is valles when it finished sending
            var requestWrapper = (RequestWrapper) ar.AsyncState;
            if (requestWrapper != null)
            {
                var clientSocket = requestWrapper.Socket;
                var clientId = requestWrapper.Id;
                var bytesSent = clientSocket.EndSend(ar); // signal that it finished
                Console.WriteLine("{0} --> Sent {1} bytes to server.", clientId, bytesSent);
            }

            // start receiving response from server
            requestWrapper?.Socket.BeginReceive(requestWrapper.Buffer, 0, RequestWrapper.BufferSize, 0, ReceivedCallback,
                requestWrapper);
        }

        private static void ReceivedCallback(IAsyncResult ar)
        {
            // retrieve data on chunks

            var requestWrapper = (RequestWrapper)ar.AsyncState;
            if (requestWrapper == null) return;
            var clientSocket = requestWrapper.Socket;
            var clientId = requestWrapper.Id;

            try
            {
                var bytesRead = clientSocket.EndReceive(ar);
                requestWrapper.ResponseContent.Append(Encoding.ASCII.GetString(requestWrapper.Buffer, 0, bytesRead)); // append this chunk to the final response 

                // if the response header has not been fully obtained, get the next chunk of data
                if (!HttpParser.ResponseHeaderObtained(requestWrapper.ResponseContent.ToString()))
                {
                    clientSocket.BeginReceive(requestWrapper.Buffer, 0, RequestWrapper.BufferSize, 0, ReceivedCallback, requestWrapper);
                }
                else
                {
                    // header has been fully obtained
                    // get the body
                    var responseBody = HttpParser.GetResponseBody(requestWrapper.ResponseContent.ToString());

                    // the custom header parser is being used to check if the data received so far has the length specified in the response headers
                    var contentLengthHeaderValue = HttpParser.GetContentLength(requestWrapper.ResponseContent.ToString());
                    if (responseBody.Length < contentLengthHeaderValue)
                    {
                        // if it isn't, than more data is to be retrieve
                        clientSocket.BeginReceive(requestWrapper.Buffer, 0, RequestWrapper.BufferSize, 0, ReceivedCallback, requestWrapper);
                    }
                    else
                    {
                        // print response 
                        Console.WriteLine(
                            "{0} --> Response received : expected {1} chars in body, got {2} chars (headers + body)",
                            clientId, contentLengthHeaderValue, requestWrapper.ResponseContent.Length);
                        clientSocket.Shutdown(SocketShutdown.Both);
                        clientSocket.Close();
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }
    }
}