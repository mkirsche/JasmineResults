/*
 * Handling a set of variants in a file and sorting by different keys (just type so far)
 */
import java.util.*;
import java.io.*;
public class VariantSet
{
	HashMap<String, Integer>[] counts;
	int size;
	static String[] names = new String[] {"SV Type"};
	
	@SuppressWarnings("unchecked")
	VariantSet()
	{
		counts = new HashMap[names.length];
		for(int i = 0; i<counts.length; i++)
		{
			counts[i] = new HashMap<String, Integer>();
		}
		size = 0;
	}
	
	void addVariant(VcfEntry entry) throws Exception
	{
		String[] keys = new String[] {entry.getNormalizedType()};
		
		for(int i = 0; i<names.length; i++)
		{
			counts[i].put(keys[i], counts[i].getOrDefault(keys[i], 0) + 1);
		}
		
		size++;
	}
	
	void print(PrintWriter out)
	{
		out.println(size);
		out.println(counts.length); // Number of partitioning schemes
		for(int i = 0; i<counts.length; i++)
		{
			out.println(names[i]);
			out.print(counts[i].keySet().size());
			for(String s : counts[i].keySet())
			{
				out.print(" " + s + " " + counts[i].get(s));
			}
			out.println();
		}
	}
	
	void printReadable()
	{
		System.out.println("Number of variants: " + size);
		System.out.println();
		for(int i = 0; i<counts.length; i++)
		{
			System.out.println(names[i]);
			for(String s : counts[i].keySet())
			{
				System.out.println("  " + s + ": " + counts[i].get(s));
			}
			System.out.println();
		}
	}
	
	/*
	 * A method for getting a map from each (optionally extended) support vector to all variants in the
	 * file which have that support vector 
	 */
	static TreeMap<String, VariantSet> fromFile(String fn, boolean useExtended) throws Exception
	{
		TreeMap<String, VariantSet> res = new TreeMap<String, VariantSet>();
		Scanner input = new Scanner(new FileInputStream(new File(fn)));
		while(input.hasNext())
		{
			String line = input.nextLine();
			if(line.length() == 0 || line.startsWith("#"))
			{
				continue;
			}
			VcfEntry entry = new VcfEntry(line);
			String suppVec = useExtended ? entry.getInfo("SUPP_VEC_EXT") : entry.getInfo("SUPP_VEC");
			res.putIfAbsent(suppVec, new VariantSet());
			res.get(suppVec).addVariant(entry);
		}
		input.close();
		
		return res;
	}
	
	/*
	 * A method for getting a map from each (optionally extended) support vector to all variants in the
	 * file which have that support vector 
	 */
	static TreeMap<String, VariantSet> fromFileSpecific(String fn, boolean useExtended) throws Exception
	{
		TreeMap<String, VariantSet> res = new TreeMap<String, VariantSet>();
		Scanner input = new Scanner(new FileInputStream(new File(fn)));
		while(input.hasNext())
		{
			String line = input.nextLine();
			if(line.length() == 0 || line.startsWith("#"))
			{
				continue;
			}
			VcfEntry entry = VcfEntry.fromLine(line);
			String suppVec = useExtended ? entry.getInfo("SUPP_VEC_EXT") : entry.getInfo("SUPP_VEC");
			
			AddGenotypes.VariantFormatField gt = new AddGenotypes.VariantFormatField(line);
			String[] isSpecifics = new String[gt.numSamples()];
			for(int i = 0; i<isSpecifics.length; i++)
			{
				isSpecifics[i] = gt.getValue(i, "IS");
			}

			// If non-specific in every individual, ignore it
			boolean isSpecific = false;
			for(int i = 0; i<isSpecifics.length; i++)
			{
				if(isSpecifics[i].equals("1")) isSpecific = true;
			}
			if(!isSpecific)
			{
				continue;
			}
			
			res.putIfAbsent(suppVec, new VariantSet());
			res.get(suppVec).addVariant(entry);
		}
		input.close();
		
		return res;
	}
}
