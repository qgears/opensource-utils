package hu.qgears.commons;

public class RationalValue {
	long nominator;
	long denominator=1;
	public RationalValue(long nominator, long denominator) {
		super();
		this.nominator = nominator;
		this.denominator = denominator;
	}
	
	public RationalValue() {
		super();
	}

	public RationalValue div(RationalValue b)
	{
		return simplify(nominator*b.denominator,
				denominator*b.nominator);
	}
	public static RationalValue simplify(long nominator, long denominator)
	{
		if(nominator==0)
		{
			return new RationalValue(nominator, denominator);
		}
		long gcd=gcd(nominator, denominator);
		return new RationalValue(nominator/gcd, denominator/gcd);
	}
	/**
	 * The greatest number that is a divisor of a and b
	 * @param a
	 * @param b
	 * @return
	 */
	public static long gcd(long a, long b)
	{
		if(b>a)
		{
			long oldA=a;
			a=b;
			b=oldA;
		}
//		long div=a/b;
		long mod=a%b;
		if(mod==0)
		{
			return b;
		}else
		{
			return gcd(b, mod);
		}
	}
	public RationalValue add(RationalValue b) {
		long sharedDenom=denominator*b.denominator;
		return simplify(nominator*b.denominator+b.nominator*denominator, sharedDenom);
	}
	@Override
	public String toString() {
		return ""+nominator+"/"+denominator;
	}
	public double toDouble()
	{
		return ((double)nominator)/denominator;
	}

	public RationalValue sub(RationalValue b) {
		long sharedDenom=denominator*b.denominator;
		return simplify(nominator*b.denominator-b.nominator*denominator, sharedDenom);
	}
}
