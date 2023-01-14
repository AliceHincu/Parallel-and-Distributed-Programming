using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Diagnostics;
using System.Net;

namespace Lab4
{
    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            var stopwatch = new Stopwatch();
            var hosts = new List<string> { "www.cs.ubbcluj.ro/~rlupsa/edu/pdp/progs/srv-begin-end.cs",
            "www.cs.ubbcluj.ro/~rlupsa/edu/pdp/progs/srv-task.cs"};

            // 1. Directly implement the parser on the callbacks (event-driven);
            var executorCallback = new CallbacksHttpExecutor(hosts);
            executorCallback.Execute();

            var executor = new SyncTasksHttpExecutor(hosts);
            executor.Execute();

            var executorAsync = new AsyncTasksHttpExecutor(hosts);
            executorAsync.Execute();
        }
    }
}