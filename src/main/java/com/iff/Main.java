package com.iff;

import com.iff.crawler.SemanticCrawler;
import com.iff.impl.SemanticCrawlerImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class Main {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        String resourceURI = "http://dbpedia.org/resource/Roberto_Ribeiro";

        SemanticCrawler crawler = new SemanticCrawlerImpl();
        crawler.search(model, resourceURI);

        model.write(System.out);
    }
}