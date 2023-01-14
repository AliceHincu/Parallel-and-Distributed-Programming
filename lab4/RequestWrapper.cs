using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Lab4
{
    public class RequestWrapper
    {
        public Socket Socket = null;

        public const int BufferSize = 1024;

        public readonly byte[] Buffer = new byte[BufferSize];

        public readonly StringBuilder ResponseContent = new StringBuilder();

        public int Id { get; set; }
        public string Hostname;
        public string Endpoint;

        public IPEndPoint RemoteEndPoint;

        public readonly ManualResetEvent ConnectionFlag = new ManualResetEvent(false);
        public readonly ManualResetEvent SentFlag = new ManualResetEvent(false);
        public readonly ManualResetEvent ReceivedFlag = new ManualResetEvent(false);
    }
}