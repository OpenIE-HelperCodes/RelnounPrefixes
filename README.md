# RelnounPrefixes

This is used while working on Relnoun(https://github.com/knowitall/chunkedextractor) to bootstrap a list of common relational noun prefixes (over a large text corpus).

It takes input POStagged & NPChunked sentences. Download the input file (1.1 million sentences of ClueWeb12 corpus) from "https://drive.google.com/file/d/0B0BIDBPb5ptqSUNUWFpOVjJpZ3M/view?usp=sharing";

It collect all words that precede relational nouns and are in the same NP chunk, are of appropriate POS (JJ, NN, NNP, etc), are not a known demonym and don’t end in a possessive. 

Refer to the paper entitled "Demonyms and Compound Relational Nouns in Nominal Open IE" (Harinder Pal, Mausam. Workshop on Automated Knowledge Base Construction (AKBC) at NAACL. San Diego, CA, USA. June 2016.) for details.
