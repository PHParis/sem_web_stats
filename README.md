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

* `hdtDirectory`: the directory where HDT files will be downloaded
* `jsonDirectory`: (outpu) the directory where JSON files containing statistics will be saved
* `datasetNamesFilePath`: The file `dataset_names` is a file containing all LOD Laundromat's datasets URIs.
* `ontologiesPath`: file containing all ontologies crawled so far, to speed up computation and avoid repeated URI dereferencing.
* `visitedUrisPath`: to speed up computation and avoid repeated URI dereferencing, we keep track of URIs we already tried to resolve in this file
* `datasetErrorsPath`: file containing datasets IDs we failed to process
* `datasetNamesLimit`: number of datasets to process. 0 is the default and means no limit
* `loadOntologies`: boolean to know if we load ontologies we already retrieved
* `forceRecomputation`: if true, then we compute again the stats about datasets having already processed
* `forceHDTDownload`: if true, then we download again the HDT file, even if it is already existing
* `passPreviousDatasetInError`: if true, datasets in error (see `datasetErrorsPath`) will be ignore. Useful when launching multiple times the tool in case of problem