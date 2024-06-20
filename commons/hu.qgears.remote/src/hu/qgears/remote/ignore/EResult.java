package hu.qgears.remote.ignore;

public enum EResult {
	impliciteOk{
		@Override
		public boolean isIgnored() {
			return false;
		}
	},
	expliciteNot{
		@Override
		public boolean isIgnored() {
			return true;
		}
	},;

	abstract public boolean isIgnored();
}
