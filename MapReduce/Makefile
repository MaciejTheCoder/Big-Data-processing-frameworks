#
# This is a simple makefile to assist with quickly building the Exercises of MP2.
#
# To build and execute a particular exercise:
#    - For a single exercise, type 'make runA' to run exercise A.
#    - For all exercises, 'make'.
#
#
HADOOP_CLASSPATH := ${JAVA_HOME}/lib/tools.jar
export HADOOP_CLASSPATH

HDFS=user/lada17/psso

OBJDIR=build

JAR := MapReducePSO.jar

TARGETS := $(addprefix run, A B C D E F)

.PHONY: final $(TARGETS) clean

final: $(TARGETS)

runA: $(OBJDIR)/TitleCount.class
	jar -cvf $(JAR) -C $(OBJDIR)/ ./
	hdfs dfs -rm -r -skipTrash -f /$(HDFS)/A-output/
	hadoop jar $(JAR) TitleCount -D stopwords=/$(HDFS)/misc/stopwords.txt -D delimiters=/$(HDFS)/misc/delimiters.txt /$(HDFS)/titles /$(HDFS)/A-output
	@echo "Run the following command to read the output file:"
	@echo "hdfs dfs -cat /$(HDFS)/A-output/part*"

runB: $(OBJDIR)/TopTitles.class
	jar -cvf $(JAR) -C $(OBJDIR)/ ./
	hdfs dfs -rm -r -skipTrash -f /$(HDFS)/B-output/
	hadoop jar $(JAR) TopTitles -D stopwords=/$(HDFS)/misc/stopwords.txt -D delimiters=/$(HDFS)/misc/delimiters.txt -D N=10 -D tmpPath=/$(HDFS)/temp /$(HDFS)/titles /$(HDFS)/B-output
	@echo "Run the following command to read the output file:"
	@echo "hdfs dfs -cat /$(HDFS)/B-output/part*"

runC: $(OBJDIR)/TopTitleStatistics.class
	jar -cvf $(JAR) -C $(OBJDIR)/ ./
	hdfs dfs -rm -r -skipTrash -f /$(HDFS)/C-output/
	hadoop jar $(JAR) TopTitleStatistics -D stopwords=/$(HDFS)/misc/stopwords.txt -D delimiters=/$(HDFS)/misc/delimiters.txt -D N=10 -D tmpPath=/$(HDFS)/temp /$(HDFS)/titles /$(HDFS)/C-output
	@echo "Run the following command to read the output file:"
	@echo "hdfs dfs -cat /$(HDFS)/C-output/part*"

runD: $(OBJDIR)/OrphanPages.class
	jar -cvf $(JAR) -C $(OBJDIR)/ ./
	hdfs dfs -rm -r -skipTrash -f /$(HDFS)/D-output/
	hadoop jar $(JAR) OrphanPages /$(HDFS)/links /$(HDFS)/D-output
	@echo "Run the following command to read the output file:"
	@echo "hdfs dfs -cat /$(HDFS)/D-output/part*"

runE: $(OBJDIR)/TopPopularLinks.class
	jar -cvf $(JAR) -C $(OBJDIR)/ ./
	hdfs dfs -rm -r -skipTrash -f /$(HDFS)/E-output/
	hadoop jar $(JAR) TopPopularLinks -D N=10 /$(HDFS)/links /$(HDFS)/E-output
	@echo "Run the following command to read the output file:"
	@echo "hdfs dfs -cat /$(HDFS)/E-output/part*"

runF: $(OBJDIR)/PopularityLeague.class
	jar -cvf $(JAR) -C $(OBJDIR)/ ./
	hdfs dfs -rm -r -skipTrash -f /$(HDFS)/F-output/
	hadoop jar $(JAR) PopularityLeague -D league=/$(HDFS)/misc/league.txt /$(HDFS)/links /$(HDFS)/F-output
	@echo "Run the following command to read the output file:"
	@echo "hdfs dfs -cat /$(HDFS)/F-output/part*"


$(OBJDIR)/%.class: %.java | $(OBJDIR)
	hadoop com.sun.tools.javac.Main $< -d $(OBJDIR)

$(OBJDIR):
	mkdir $@

.PHONY: clean
clean:
	rm -f $(OBJDIR)/* $(JAR)
	hdfs dfs -rm -r -skipTrash -f /$(HDFS)/*-output/
