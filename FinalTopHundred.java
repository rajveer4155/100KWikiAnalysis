import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class FinalTopHundred {
	static TreeMap <String, Integer> keyFreqPairsMap = new TreeMap();
	static TreeMap <String, Integer> keyFreqSinglesMap = new TreeMap();
	static TreeMap <String, Integer>  RelFreqPairsMap = new TreeMap();
	static TreeMap <String, Integer> TopHundredPairsMap = new TreeMap();
  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	String indWords[]=value.toString().split("\\s+");
    	for(int i=0;i<indWords.length;i++){
    		if((i+1)>=indWords.length){
    			break;
    		}
    		word.set(indWords[i] + " " +indWords[i+1]);
            context.write(word, one);
    		///context.write(indWords[i], one);
    	}
    	for(int j=0;j<indWords.length;j++){
    		
    		word.set(indWords[j]);
            context.write(word, one);
    		///context.write(indWords[i], one);
    	}
      /*StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }*/
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
    	int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      
      //context.write(key, result);
      //Checking if its Pair
      String [] SplitArr=key.toString().split(" ");
      if(SplitArr.length==2){
    	  keyFreqPairsMap.put(key.toString(), sum);
      }else if(SplitArr.length==1){
    	  keyFreqSinglesMap.put(key.toString(), sum);
      }
      
    } // Reduce Function ended
    
    //Cleanup
    protected void cleanup(Context context) throws IOException, InterruptedException {
  	// calculating top hundred in a tree map
    	try{
        for (Map.Entry<String, Integer> entry : keyFreqPairsMap.entrySet()) {  
      	  String pairsKey=entry.getKey();
      	  int pairsFreq=entry.getValue();
      	  //System.out.println("Splitting Key RelFreq: "+pairsKey);
      	  String [] firstWord=pairsKey.split(" ");
      	  
      	  int relativeFreq=pairsFreq /keyFreqSinglesMap.get(firstWord[0]);
      	  //System.out.println("RelFreq: "+relativeFreq);
      	  //**** Writing to TopHundredMap
      	  RelFreqPairsMap.put(pairsKey, relativeFreq);
      	 
  	     } // For Loop ends
        
        TopHundredPairsMap=sortByValues(RelFreqPairsMap);
  	  System.out.println("Starting Printing top 100");
  	  //Printing Top 100 START
  	  int count=0;
  	  for (Map.Entry<String, Integer> topHundredEntry : TopHundredPairsMap.entrySet()) { 
  		  
  		  if(count==100){
  			  break;
  		  }
  		  //System.out.println("Starting Printing top "+ count +"; KEY: "+topHundredEntry.getKey());
  		  context.write(new Text(topHundredEntry.getKey()),new IntWritable(topHundredEntry.getValue()));
  	      count++;
  	  } //printing 100 for loop ends
    	}catch(Exception e){
    		System.out.println("Exception in Cleanup: "+ e.getMessage());
    	}
    }
    	
  }
  
  public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValues(final TreeMap<K, V> map) {
	    Comparator<K> valueComparator =  new Comparator<K>() {
	        public int compare(K k1, K k2) {
	            int compare = map.get(k2).compareTo(map.get(k1));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    TreeMap<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}
  
 
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(FinalTopHundred.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}