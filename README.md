# LOD statistics

All this work is based on the [HDT library](https://github.com/rdfhdt/hdt-java).

## Overview

Nowadays, there is a vast quantity of LD datasets available on the Web. Those datasets are often expressed in RDF allowing us to infer new data. Semantics contained in those datasets may help an automated agent to infer these new data. For example, a transitive property may lead to new assertions. But, in the wild, is semantics that much present? In other words, is the Linked Data semantics data, or is it data linked together with only a few semantics sprinkled on it? In this paper, we propose an in-depth and large-scale exploration of Linked Data datasets to understand how semantics is used. This study shows that there is a real lack of semantics. Hence, we suggest some ideas that help to improve semantics within Linked Data and to manage the existing situation.

This tool aims at computing statistics about Linked Data datasets.

## Installation and configuration

To compile/install you can use the following commands: `mvn clean compile` and `mvn clean install`.

Under `hdt-java-package` directory, you can run the `mvn assembly:single` command to generate a distribution directory with all the jars and launcher scripts.

To launch the application, you can run the following class with the specified JSON configuration file: `fr.cnam.ph.Stats conf.json`

The configuration file `conf.json`:

```json
{
  "hdtDirectory": "path/to/where/hdt/files/will/be/downloaded",
  "jsonDirectory": "output/path",
  "datasetNamesFilePath": "file/containing/dataset_names",
  "ontologiesPath": "file/containing/ontologies.nt",
  "visitedUrisPath": "file/containing/visited/uris/of/ontologies",
  "datasetErrorsPath": "file/containing/dataset/with/errors/while/processing",
  "datasetNamesLimit": 0,
  "loadOntologies": true,
  "forceRecomputation": false,
  "forceHDTDownload": false,
  "passPreviousDatasetInError": true
}
```

- `hdtDirectory`: the directory where HDT files will be downloaded
- `jsonDirectory`: (outpu) the directory where JSON files containing statistics will be saved
- `datasetNamesFilePath`: The file `dataset_names` is a file containing all LOD Laundromat's datasets URIs.
- `ontologiesPath`: file containing all ontologies crawled so far, to speed up computation and avoid repeated URI dereferencing.
- `visitedUrisPath`: to speed up computation and avoid repeated URI dereferencing, we keep track of URIs we already tried to resolve in this file
- `datasetErrorsPath`: file containing datasets IDs we failed to process
- `datasetNamesLimit`: number of datasets to process. 0 is the default and means no limit
- `loadOntologies`: boolean to know if we load ontologies we already retrieved
- `forceRecomputation`: if true, then we compute again the stats about datasets having already processed
- `forceHDTDownload`: if true, then we download again the HDT file, even if it is already existing
- `passPreviousDatasetInError`: if true, datasets in error (see `datasetErrorsPath`) will be ignore. Useful when launching multiple times the tool in case of problem

## Results

Descriptions of the different selectors:
|Name | Description |
|-----|------------|
|ALL | all datasets|
|With topic | all datasets with a topic that is not "unknown"|
|Without topic | all datasets with an "unknown" topic|
|With semantics | all datasets with at least one OWL or RDFS semantic feature|
|With semantics and topic | all datasets with a topic that is not "unknown" & and at least one OWL or RDFS semantic feature|
|TOP 100 | the top 100 datasets in terms of number of triples|
|Topic | all datasets having this topic|

Basic statistics by selector in terms of number of subjects (first quartile, median and third quartile) and percentage of datasets using OWL:
|Selector | # of datasets | Q1 | Q2 | Q3 | % of datasets w/ one OWL feature |
|---------|---------------|----|----|----|----------------------------------|
ALL | 647,858 | 547 | 3146.5 | 38,580 | 1.53 |
w/ topic | 706 | 8 | 29.5 | 425.25 | 37.54 |
w/o topic | 647,152 | 562 | 3301 | 39,126.5 | 1.49 |
w/ semantics | 10,600 | 184 | 3952 | 93,891 | 100 |
w/ sem \& topic | 271 | 8.25 | 62.5 | 1496.25 | 100 |
TOP 100 (# triples) | 100 | 5,611,982.25 | 8,951,766 | 12,635,325.5 | 34 |

Percentages of subjects without types, and predicates without domains and/or ranges:
|Selector | % of subjects w/o types | % of predicates w/o domain | % of predicates w/o range |
|---|---|---|---|
|ALL | 41.08 | 99.68 | 99.67 |
|w/ topic | 6.49 | 98.84 | 96.29 |
|w/o topic | 41.49 | 99.68 | 99.67 |
|w/ semantics | 22.36 | 98.84 | 98.79 |
|w/ sem \& topic | 4.45 | 98 | 93.6 |
|TOP 100 | 12.45 | 99.69 | 99.69 |

Basic statistics by topics in terms of number of subjects (first quartile, median and third quartile) and percentage of datasets using OWL:
|Selector | # of datasets | Q1 | Q2 | Q3 | % of datasets w/ one OWL feature |
|---|---|---|---|---|---|
|publications | 217 | 8.5 | 36 | 252.5 | 40.55 |
|government | 137 | 4.25 | 49.5 | 473.75 | 30.66 |
|user_generated | 91 | 8 | 18 | 39.5 | 12.09 |
|life_sciences | 68 | 2 | 21 | 39 | 35.29 |
|cross_domain | 65 | 16.75 | 48.5 | 208.5 | 64.62 |
|geography | 48 | 2 | 8 | 443 | 43.75 |
|linguistics | 43 | 1 | 20 | 657 | 48.84 |
|media | 29 | 2 | 8 | 110 | 51.72 |
|social_networking | 8 | 3 | 13 | 37 | 12.50 |

Percentages of subjects without types, of predicates without domains and/or ranges of datasets by topic:
| Selector | \% of subjects w/o types | \% of predicates w/o domain | \% of predicates w/o range |
|---|---|---|---|
| publications | 5.91 | 96.69 | 93.11 |
| government | 8.48 | 99.87 | 99.64 |
| user_generated | 79.82 | 99.49 | 94.51 |
| life_sciences | 0.02 | 100 | 100 |
| cross_domain | 13.24 | 99.39 | 99.12 |
| geography | 82.83 | 98.06 | 92 |
| linguistics | 3.05 | 100 | 100 |
| media | 41.13 | 100 | 100 |
| social_networking | 0.31 | 100 | 100 |

Analysis of types of properties:
| feature | # of datasets | weighted mean of triples | weighted mean of subjects | weighted mean of predicates |
|---|---|---|---|---|
| ObjectProperty | 747 | 27.5 | 16.57 | 7.53 |
| DatatypeProperty | 685 | 31 | 19.43 | 5.79 |
| FunctionalProperty | 434 | 9 | 5.76 | 3.06 |
| InverseFunctionalProperty | 310 | 22.7 | 20.6 | 2.54 |
| TransitiveProperty | 396 | 2.84 | 2.63 | 2.4 |
| SymmetricProperty | 320 | 7 | 4.77 | 2.87 |
| AsymmetricProperty | 15 | 4.7 | 4.66 | 4.66 |
| IrreflexiveProperty | 21 | 1.66 | 1.65 | 1.65 |
| ReflexiveProperty | 16 | 1.32 | 1.32 | 1.32 |

<!-- Definition (**Property and class usage**):
Let \\(C\\) be an OWL class (resp. $$p$$ an OWL property).
Let \(\Omega=\lbrace KB_i|i\in[1,N]\rbrace\) be a set of datasets of cardinality \(N\).

The **class usage** (resp. the **property usage**) of a class \(C\) (resp. property \(p\)), noted \(CU*{\Omega}(C)\) (resp. \(PU*{\Omega}(p)\)), is the weighted mean of the number of subjects having \(C\) as an RDF type (resp. using \(p\) as a predicate).
The weights are the inverse of the total number of subjects in the dataset.

    \(CU_{\Omega}(C)=\frac{\sum_{i=1}^{N}|Sub(KB_i, C)|\times \frac{1}{|Sub(KB_i)|}}{\sum_{i=1}^{N}\frac{1}{|Sub(KB_i)|}}\)

\begin{equation}
PU*{\Omega}(p)=\frac{\sum*{i=1}^{N}|Sub(KB*i, p)|\times \frac{1}{|Sub(KB_i)|}}{\sum*{i=1}^{N}\frac{1}{|Sub(KB_i)|}}
\end{equation}
where \(Sub(KB_i, C)\) gives all distinct subjects of type \(C\) in \(KB_i\),
\(Sub(KB_i, p)\) gives all distinct subjects having the property \(p\) in \(KB_i\)
and \(Sub(KB_i)\) gives all distinct subjects in \(KB_i\).

This definition prevents large datasets to push the mean to a large amount. -->

Analysis of OWL classes (see previous definition}):
| feature | # of datasets | Class Usage |
|---|---|---|
| Ontology | 2197 | 1.01 |
| Class | 1905 | 1.36 |
| Restriction | 520 | 10.3 |
| DataRange | 225 | 1.71 |
| AllDifferent | 213 | 2.35 |
| DeprecatedClass | 126 | 6.95 |
| NamedIndividual | 62 | 10.8 |
| AllDisjointClasses | 50 | 2.09 |
| NegativePropertyAssertion | 27 | 279.76 |
| Axiom | 13 | 14.44 |
| AllDisjointProperties | 5 | 4.96 |

Analysis of OWL properties (see previous definition):
| feature | # of datasets | Property Usage |
| --- | --- |--- |
| sameAs | 7708 | 10.30 |
| unionOf | 1256 | 1.02 |
| inverseOf | 548 | 5.41 |
| onProperty | 522 | 10.19 |
| equivalentClass | 492 | 5.11 |
| disjointWith | 470 | 2.49 |
| allValuesFrom | 425 | 7.92 |
| someValuesFrom | 413 | 10.07 |
| cardinality | 412 | 3.1 |
| minCardinality | 398 | 4.26 |
| intersectionOf | 394 | 5.26 |
| maxCardinality | 388 | 4.88 |
| oneOf | 364 | 1.99 |
| hasValue | 348 | 5.58 |
| equivalentProperty | 324 | 4.61 |
| complementOf | 315 | 1.82 |
| distinctMembers | 207 | 2.26 |
| differentFrom | 144 | 30.1 |
| onClass | 91 | 2.99 |
| members | 55 | 2.13 |
| propertyChainAxiom | 38 | 1.76 |
| onDataRange | 32 | 7.2 |
| qualifiedCardinality | 32 | 2.63 |
| assertionProperty | 27 | 279.76 |
| sourceIndividual | 27 | 279.76 |
| targetIndividual | 27 | 279.76 |
| minQualifiedCardinality | 25 | 4.24 |
| disjointUnionOf | 24 | 2.13 |
| withRestrictions | 13 | 1.04 |
| annotatedTarget | 12 | 14.83 |
| maxQualifiedCardinality | 12 | 4.06 |
| annotatedProperty | 10 | 12.71 |
| annotatedSource | 10 | 12.71 |
| onDatatype | 10 | 1.16 |
| hasKey | 8 | 1 |
| propertyDisjointWith | 5 | 1.08 |
| hasSelf | 2 | 1.88 |
| datatypeComplementOf | 1 | 1 |
