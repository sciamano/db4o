namespace com.db4o.inside.query
{
	public class QueryOptimizationFailureEventArgs : System.EventArgs
	{
		System.Exception _reason;

		public QueryOptimizationFailureEventArgs(System.Exception e)
		{
			_reason = e;
		}

		public System.Exception Reason
		{
			get { return _reason; }
		}
	}

	public delegate void QueryOptimizationFailureHandler(object sender, QueryOptimizationFailureEventArgs args);
}