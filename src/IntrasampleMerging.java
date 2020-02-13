import java.util.*;
import java.io.*;
public class IntrasampleMerging {
	static String inputFile = "";
	static String outputFile = "";
public static void main(String[] args) throws Exception
{
	if(args.length != 2)
	{
		System.out.println("Usage: java IntrasampleMerging input_vcf output_vcf");
		return;
	}
	else
	{
		inputFile = args[0];
		outputFile = args[2];
		convertFile(inputFile, outputFile);
	}
}
static void convertFile(String inputFile, String outputFile) throws Exception
{
	Scanner input = new Scanner(new FileInputStream(new File(inputFile)));
	PrintWriter out = new PrintWriter(new File(outputFile));
	
	VcfHeader header = new VcfHeader();
	ArrayList<VcfEntry> entries = new ArrayList<VcfEntry>();
	
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.length() == 0)
		{
			continue;
		}
		else if(line.startsWith("#"))
		{
			header.addLine(line);
		}
		else
		{
			entries.add(new VcfEntry(line));
		}
	}
	
	input.close();
	out.close();
}
/*
 * The equivalent of a compareTo method for VcfEntries which sorts by chromosome, then type, then position, then id
 */
static int compareEntries(VcfEntry first, VcfEntry second) throws Exception
{
	String chr1 = first.getChromosome();
	String chr2 = second.getChromosome();
	if(!chr1.equals(chr2))
	{
		return chr1.compareTo(chr2);
	}
	String type1 = first.getType(), type2 = second.getType();
	if(!type1.equals(type2))
	{
		return type2.compareTo(type2);
	}
	long pos1 = first.getPos();
	long pos2 = first.getPos();
	if(pos1 != pos2)
	{
		return Long.compare(pos1, pos2);
	}
	return first.getId().compareTo(second.getId());
}
}
