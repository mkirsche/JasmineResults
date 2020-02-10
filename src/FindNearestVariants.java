import java.util.*;
import java.io.*;
public class FindNearestVariants {
public static void main(String[] args) throws Exception
{
	String toCheckFn = "/home/mkirsche/giab/discordantinsertions.vcf";
	String[] otherVcfs = new String[] {
		"/home/mkirsche/giab/hg002.vcf",
		"/home/mkirsche/giab/hg003.vcf",
		"/home/mkirsche/giab/hg004.vcf"
	};
	
	String[] names = new String[] {"hg002", "hg003", "hg004"};
	
	VariantMap[] maps = new VariantMap[otherVcfs.length];
	for(int i = 0; i<otherVcfs.length; i++) maps[i] = new VariantMap(otherVcfs[i]);
	
	Scanner input = new Scanner(new FileInputStream(new File(toCheckFn)));
	
	PrintWriter out = new PrintWriter(new File("insertionMatches.txt"));
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.length() == 0 || line.startsWith("#")) continue;
		VcfEntry entry = new VcfEntry(line);
		out.println("Bad insertion: " + line);
		for(int i = 0; i<names.length; i++)
		{
			out.println("  " + names[i]);
			String[] near = maps[i].getNearest(entry);
			for(String s : near) out.println("    " + s);
		}
		out.println();
	}
	input.close();
	out.close();
}
static class VariantMap
{
	HashMap<String, TreeMap<Long, String>> map;
	VariantMap()
	{
		map = new HashMap<String, TreeMap<Long, String>>();
	}
	VariantMap(String fn) throws Exception
	{
		Scanner input = new Scanner(new FileInputStream(new File(fn)));
		map = new HashMap<String, TreeMap<Long, String>>();
		while(input.hasNext())
		{
			String line = input.nextLine();
			if(line.length() == 0 || line.startsWith("#")) continue;
			add(line);
		}
		input.close();
	}
	
	void add(String line) throws Exception
	{
		VcfEntry entry = new VcfEntry(line);
		String chr = entry.getChromosome();
		long pos = entry.getPos();
		if(!map.containsKey(chr)) map.put(chr, new TreeMap<Long, String>());
		if(map.get(chr).containsKey(pos)) map.get(chr).put(pos, map.get(chr).get(pos) + "\n" + line);
		else map.get(chr).put(pos, line);
	}
	
	String[] getNearest(VcfEntry entry) throws Exception
	{
		String chr = entry.getChromosome();
		long pos = entry.getPos();
		
		ArrayList<String> nearby = new ArrayList<String>();
		
		TreeMap<Long, String> sameChr = map.get(chr);
		Long ceilingKey = sameChr.higherKey(pos);
		Long floorKey = sameChr.floorKey(pos);
		if(floorKey != null && sameChr.lowerKey(floorKey) != null)
		{
			nearby.add(sameChr.get(sameChr.lowerKey(floorKey)));
		}
		if(floorKey != null) nearby.add(sameChr.get(floorKey));
		if(ceilingKey != null) nearby.add(sameChr.get(ceilingKey));
		if(ceilingKey != null && sameChr.higherKey(ceilingKey) != null)
		{
			nearby.add(sameChr.get(sameChr.higherKey(ceilingKey)));
		}
		
		
		String[] res = new String[nearby.size()];
		for(int i = 0; i<res.length; i++) res[i] = nearby.get(i);
		
		return res;
	}
}
}
