import java.util.*;
import java.io.*;
public class TrioResults {
	static boolean requireSpecific = true;
public static void main(String[] args) throws Exception
{
	String fn = "/home/mkirsche/giab/merged_gt.vcf";
	String ofn = "trio.txt";
	TreeMap<String, VariantSet> bySuppVec = requireSpecific ? VariantSet.fromFileSpecific(fn, false) : VariantSet.fromFile(fn, false);
	int firstOnly = bySuppVec.getOrDefault("100", new VariantSet()).size;
	int secondOnly = bySuppVec.getOrDefault("011", new VariantSet()).size;
	int both = bySuppVec.getOrDefault("111", new VariantSet()).size;
	System.out.println("Child only: " +  firstOnly);
	System.out.println("Both parents only: " + secondOnly);
	System.out.println("All three samples: " + both);
	System.out.println();
	
	PrintWriter out = new PrintWriter(new File(ofn));
	//out.println(3);
	out.println(bySuppVec.size());
	for(String s : bySuppVec.keySet())
	{
		System.out.println("Support vector: " + s);
		bySuppVec.get(s).printReadable();
		
		out.println(s);

		bySuppVec.get(s).print(out);
	}
	out.close();
	
	mendelianDiscordance(fn);
}

static void mendelianDiscordance(String fn) throws Exception
{
	Scanner input = new Scanner(new FileInputStream(new File(fn)));
	
	int count = 0;
	int bad = 0;
	int filtered = 0;
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.length() == 0 || line.startsWith("#")) continue;
		VcfEntry entry = VcfEntry.fromLine(line);
		String support = entry.getInfo("SUPP_VEC");
		AddGenotypes.VariantFormatField gt = new AddGenotypes.VariantFormatField(line);
		String[] isSpecifics = new String[gt.numSamples()];
		for(int i = 0; i<isSpecifics.length; i++)
		{
			isSpecifics[i] = gt.getValue(i, "IS");
		}
		
		// If non-specific in both parents, ignore it
		if(!isSpecifics[0].equals("1") && !isSpecifics[1].equals("1") && !isSpecifics[2].equals("1"))
		{
			filtered++;
			continue;
		}
		
		if(support.equals("011"))
		{
			bad++;
			count++;
		}
		else
		{
			count++;
		}
	}
	System.out.println(bad+" "+filtered+" "+count);
	input.close();
}
}
