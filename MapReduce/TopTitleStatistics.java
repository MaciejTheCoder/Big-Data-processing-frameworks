import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Integer;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

// Don't Change >>>
public class TopTitleStatistics extends Configured implements Tool {
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TopTitleStatistics(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();
        Path tmpPath = new Path( conf.get("tmpPath") );
        FileSystem fs = FileSystem.get(conf);
        fs.delete(tmpPath, true);

        Job jobA = Job.getInstance(conf, "Title Count");
        jobA.setOutputKeyClass(Text.class);
        jobA.setOutputValueClass(IntWritable.class);

        jobA.setMapperClass(TitleCountMap.class);
        jobA.setReducerClass(TitleCountReduce.class);
        jobA.setNumReduceTasks(2);
        
        FileInputFormat.setInputPaths(jobA, new Path(args[0]));
        FileOutputFormat.setOutputPath(jobA, tmpPath);

        jobA.setJarByClass(TopTitleStatistics.class);
        boolean result = jobA.waitForCompletion(true);

    if(result) {
          Job jobB = Job.getInstance(conf, "Top Titles Statistics");
          jobB.setOutputKeyClass(Text.class);
          jobB.setOutputValueClass(IntWritable.class);

          jobB.setMapOutputKeyClass(NullWritable.class);
          jobB.setMapOutputValueClass(IntWritable.class);

          jobB.setMapperClass(TopTitlesStatMap.class);
          jobB.setReducerClass(TopTitlesStatReduce.class);
          jobB.setNumReduceTasks(1);

          FileInputFormat.setInputPaths(jobB, tmpPath);
          FileOutputFormat.setOutputPath(jobB, new Path(args[1]));

          jobB.setInputFormatClass(KeyValueTextInputFormat.class);
          jobB.setOutputFormatClass(TextOutputFormat.class);

          jobB.setJarByClass(TopTitleStatistics.class);

      result = jobB.waitForCompletion(true);
        }

        fs.delete(tmpPath, true);

        return (result) ? 0 : 1;
    }

    public static String readHDFSFile(String path, Configuration conf) throws IOException{
        Path pt=new Path(path);
        FileSystem fs = FileSystem.get(pt.toUri(), conf);
        FSDataInputStream file = fs.open(pt);
        BufferedReader buffIn=new BufferedReader(new InputStreamReader(file));

        StringBuilder everything = new StringBuilder();
        String line;
        while( (line = buffIn.readLine()) != null) {
            everything.append(line);
            everything.append("\n");
        }
        return everything.toString();
    }


// <<< Don't Change

    public static class TitleCountMap extends Mapper<Object, Text, Text, IntWritable> {
        private List<String> stopWords;
        private String delimiters;
        private final static IntWritable one = new IntWritable(1);

        @Override
        protected void setup(Context context) throws IOException,InterruptedException {

            Configuration conf = context.getConfiguration();

            String stopWordsPath = conf.get("stopwords");
            String delimitersPath = conf.get("delimiters");

            this.stopWords = Arrays.asList(readHDFSFile(stopWordsPath, conf).split("\n"));
            this.delimiters = readHDFSFile(delimitersPath, conf);
        }


        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // TODO
            String valueString = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(valueString,delimiters);
        while(tokenizer.hasMoreTokens()){
            String nextToken = tokenizer.nextToken().trim().toLowerCase();
            if(!stopWords.contains(nextToken)){
                context.write(new Text(nextToken), new IntWritable(1));
            }
        }
        

        }
    }

    public static class TitleCountReduce extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            // TODO
            int counter = 0;

        for(IntWritable val:values){
            counter +=val.get();
        }
        IntWritable c = new IntWritable(counter);
        context.write(key,c);

        }
    }

    public static class TopTitlesStatMap extends Mapper<Text, Text, NullWritable, IntWritable> {
        private Integer N;
    private IntWritable outValue = new IntWritable(0);
    private TreeSet<ComparablePair<Integer, String>> countToTitleMap = new TreeSet<ComparablePair<Integer, String>>();

        // TODO

        @Override
        protected void setup(Context context) throws IOException,InterruptedException {
            Configuration conf = context.getConfiguration();
            this.N = conf.getInt("N", 10);
        }

        @Override
        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            // TODO
            countToTitleMap.add(new ComparablePair<>(Integer.valueOf(value.toString()), key.toString()));
            // adding elements -> ASCENDING ORDER
            if(countToTitleMap.size() > this.N) {
                countToTitleMap.remove(countToTitleMap.first());
            }

        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            // TODO
            for(ComparablePair<Integer, String> x:countToTitleMap){
                IntWritable key = new IntWritable(x.getKey());
                Text value = new Text(x.getValue());
                context.write(NullWritable.get(),key);

            }

        }
    }

    public static class TopTitlesStatReduce extends Reducer<NullWritable, IntWritable, Text, IntWritable> {
        private Integer N;
        // TODO
        private TreeSet<ComparablePair<Integer, String>> countToTitleMap = new TreeSet<ComparablePair<Integer, String>>();


        @Override
        protected void setup(Context context) throws IOException,InterruptedException {
            Configuration conf = context.getConfiguration();
            this.N = conf.getInt("N", 10);
        }

        @Override
        public void reduce(NullWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            Integer sum, mean, max, min, var;
            sum = 0;
            mean = 0;
            max = 0;
            min = 0;
            var = 0;

            // TODO

            for(IntWritable val:values){
                countToTitleMap.add(new ComparablePair<>(val.get(), key.toString()));
                if(countToTitleMap.size() > this.N) {
                    countToTitleMap.remove(countToTitleMap.first());
                }
            } 

            min = countToTitleMap.first().getKey();
            max = countToTitleMap.last().getKey();

            for(ComparablePair<Integer, String> x:countToTitleMap){
                Integer k = x.getKey();
                sum += k;
                /*if(k>max){
                    max = k;
                }
                if(k<min){
                    min = k;
                }*/
            }
            mean = sum/this.N;

            for(ComparablePair<Integer,String> x:countToTitleMap){
                Integer k = x.getKey();
                Integer temp = (k - mean);
                temp = temp*temp;
                var += temp;
                
            }

            var = var/(this.N);

            Text sumName= new Text();
            sumName.set("Sum:");
            IntWritable sumW = new IntWritable(sum);
            context.write(sumName,sumW);

            Text meanName= new Text();
            meanName.set("Mean:");
            IntWritable meanW = new IntWritable(mean);
            context.write(meanName,meanW);

            Text maxName= new Text();
            maxName.set("Max:");
            IntWritable maxW = new IntWritable(max);
            context.write(maxName,maxW);

            Text minName= new Text();
            minName.set("Min:");
            IntWritable minW = new IntWritable(min);
            context.write(minName,minW);

            Text varName= new Text();
            varName.set("Var:");
            IntWritable varW = new IntWritable(var);
            context.write(varName,varW);
        }
    }

}

// >>> Don't Change
class ComparablePair<K extends Comparable<? super K>, V extends Comparable<? super V>>
    extends javafx.util.Pair<K,V> 
    implements Comparable<ComparablePair<K, V>> {

    public ComparablePair(K key, V value) {
    super(key, value);
    }

    @Override
    public int compareTo(ComparablePair<K, V> o) {
        int cmp = o == null ? 1 : (this.getKey()).compareTo(o.getKey());
        return cmp == 0 ? (this.getValue()).compareTo(o.getValue()) : cmp;
    }

}
// <<< Don't Change
