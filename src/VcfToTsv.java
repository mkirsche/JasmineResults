import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

public class VcfToTsv {
public static void main(String[] args) throws Exception
{
	String fn = args[0];
	Scanner input = new Scanner(new FileInputStream(new File(fn)));
	String ofn = fn.substring(0,fn.length() - 3) + "tsv";
	PrintWriter out = new PrintWriter(new File(ofn));
	TreeSet<String> infoFieldNames = new TreeSet<String>();
	infoFieldNames.add("IS_PRECISE");
	int maxTokens = 0;
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.length() == 0 || line.startsWith("#"))
		{
			continue;
		}
		String[] tabTokens = line.split("\t");
		maxTokens = Math.max(maxTokens,  tabTokens.length);
		String infoToken = tabTokens[7];
		String[] infos = infoToken.split(";");
		for(String info : infos)
		{
			int equalsIdx = info.indexOf('=');
			if(equalsIdx == -1)
			{
				continue;
			}
			String key = info.substring(0, equalsIdx);
			//String val = info.substring(1 + equalsIdx);
			if(!infoFieldNames.contains(key))
			{
				infoFieldNames.add(key);
			}
		}
	}
	input.close();
	ArrayList<String> headerLines = new ArrayList<String>();
	input = new Scanner(new FileInputStream(new File(fn)));
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.length() == 0)
		{
			continue;
		}
		if(line.startsWith("#CHROM"))
		{
			String[] tokens = line.substring(1).split("\t");
			for(String token : tokens)
			{
				if(!token.equals("INFO"))
				{
					headerLines.add(token);
				}
			}
			
			for(String infoName : infoFieldNames)
			{
				headerLines.add(infoName);
			}
			
			out.print(headerLines.get(0));
			for(int i = 1; i<headerLines.size(); i++)
			{
				out.print("\t" + headerLines.get(i));
			}
			out.println();
		}
		if(line.startsWith("#"))
		{
			continue;
		}
		
		// Now process the current line
		
		// Gather its INFO fields
		String[] tokens = line.split("\t");
		HashMap<String, String> infoData = new HashMap<String, String>();
		String infoToken = tokens[7];
		String[] infos = infoToken.split(";");
		for(String info : infos)
		{
			int equalsIdx = info.indexOf('=');
			if(equalsIdx == -1)
			{
				if(info.equalsIgnoreCase("PRECISE"))
				{
					infoData.put("IS_PRECISE", "1");
				}
				else if(info.equalsIgnoreCase("IMPRECISE"))
				{
					infoData.put("IS_PRECISE", "0");
				}
				continue;
			}
			String key = info.substring(0, equalsIdx);
			String val = info.substring(1 + equalsIdx);
			infoData.put(key, val);
		}
		
		// Now print out the line
		ArrayList<String> allData = new ArrayList<String>();
		for(int i = 0; i<tokens.length; i++)
		{
			if(i != 7)
			{
				allData.add(tokens[i]);
			}
		}
		for(int i = 0; i<maxTokens - tokens.length; i++)
		{
			allData.add(".");
		}
		for(String field : infoFieldNames)
		{
			if(infoData.containsKey(field))
			{
				allData.add(infoData.get(field));
			}
			else
			{
				allData.add(".");
			}
		}
		out.print(allData.get(0));
		for(int i = 1; i<allData.size(); i++) out.print("\t" + allData.get(i));
		out.println();
	}
	input.close();
	out.close();
	
}
}
