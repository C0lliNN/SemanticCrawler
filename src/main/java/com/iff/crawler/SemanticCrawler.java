package com.iff.crawler;

import org.apache.jena.rdf.model.Model;

public interface SemanticCrawler {
    void search(Model model, String resourceURI);
}
