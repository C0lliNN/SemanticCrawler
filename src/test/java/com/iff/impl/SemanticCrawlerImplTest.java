package com.iff.impl;

import com.iff.crawler.SemanticCrawler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class SemanticCrawlerImplTest {

    @Test
    void search() {
        Model expected = ModelFactory.createDefaultModel();
        FileManager.getInternal().readModelInternal(expected, "src/test/resources/Roberto_Ribeiro.rdf");

        Model model = ModelFactory.createDefaultModel();
        String resourceURI = "http://dbpedia.org/resource/Roberto_Ribeiro";

        SemanticCrawler crawler = new SemanticCrawlerImpl();
        crawler.search(model, resourceURI);

        model.write(System.out);

        assertTrue(expected.isIsomorphicWith(model));
    }
}