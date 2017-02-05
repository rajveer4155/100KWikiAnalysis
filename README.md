## 100KWikiAnalysis - Analysis of 100K Wiki Articles using Hadoop Map-Reduce.

This Program uses MapReduce-based approach in Hadoop system to compute the relative frequencies of each word that occurs in all the documents in 100KWikiText.txt, and output the top 100 word pairs sorted in a decreasing order of relative frequency. 

##### Note that the relative frequency (RF) of word B given word A is defined as follows:
###### f(B|A)= Count(A,B) / Count(A) = Count(A,B) / ΣB' Count(A,B')
where count(A,B) is the number of times A and B co-occur in a document, and count(A) the number of times A occurs with anything else. Intuitively, given a document collection, the relative frequency captures the proportion of time the word B appears in the same document as A.

### Input file: 100KWikiText.txt

### Instructions to run:
1. Compile the code and export it as jar file.
2. Run it using command: 
  $ bin/hadoop jar <jarfileName> <inputFiles> <outputLocation>
