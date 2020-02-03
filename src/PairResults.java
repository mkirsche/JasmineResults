/*
 * Gets the results of pairwise merging partitioned by the properties listed in VariantSet.java
 */
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
public class PairResults {
public static void main(String[] args) throws Exception
{
	String fn = "/home/mkirsche/eclipse-workspace/Jasmine/entex.vcf";
	String ofn = "pair.txt";
	if(args.length == 2)
	{
		//String fn = args[0];
		//String ofn = args[1];
	}
	TreeMap<String, VariantSet> bySuppVec = VariantSet.fromFile(fn, false);
	int firstOnly = bySuppVec.getOrDefault("10", new VariantSet()).size;
	int secondOnly = bySuppVec.getOrDefault("01", new VariantSet()).size;
	int both = bySuppVec.getOrDefault("11", new VariantSet()).size;
	System.out.println("First sample only: " +  firstOnly);
	System.out.println("Second sample only: " + secondOnly);
	System.out.println("Both samples: " + both);
	System.out.println();
	
	PrintWriter out = new PrintWriter(new File(ofn));
	out.println(bySuppVec.size());
	for(String s : bySuppVec.keySet())
	{
		System.out.println("Support vector: " + s);
		bySuppVec.get(s).printReadable();
		if(s.equals("01")) out.println("Second Sample Only");
		else if(s.equals("10")) out.println("First Sample Only");
		else if(s.equals("11")) out.println("Shared");
		else out.println(s);
		bySuppVec.get(s).print(out);
	}
	out.close();
}
}
