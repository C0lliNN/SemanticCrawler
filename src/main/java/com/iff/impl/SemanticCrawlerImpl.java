package com.iff.impl;

import com.iff.crawler.SemanticCrawler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class SemanticCrawlerImpl implements SemanticCrawler {
    private ArrayDeque<String> urisToBeVisited;
    private Set<String> visitedUris;

    public SemanticCrawlerImpl() {
        this.urisToBeVisited = new ArrayDeque<>();
        this.visitedUris = new HashSet<>();
    }

    @Override
    public void search(final Model outputModel, final String resourceURI) {
        try {
            System.out.println("Searching URI: " + resourceURI);

            visitedUris.add(resourceURI);

            Model model = ModelFactory.createDefaultModel();
            // Step 1
            model.read(resourceURI);

            // Step 2
            StmtIterator stmtIterator = model.listStatements(ResourceFactory.createResource(resourceURI), null, (RDFNode) null);
            outputModel.add(stmtIterator);

            // Step 3: <URI> <algumaPropriedade> <algumValor> .
            stmtIterator = model.listStatements(ResourceFactory.createResource(resourceURI), OWL.sameAs, (RDFNode) null);
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                Resource object = statement.getObject().asResource();
                if (object.isAnon()) {
                    searchAnonymousNode(outputModel, object);
                } else {
                    urisToBeVisited.push(object.getURI());
                }
            }

            // Step 3: <recursoSujeito> owl:sameAs <URI> .
            stmtIterator = model.listStatements(null, OWL.sameAs, ResourceFactory.createResource(resourceURI));
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                Resource subject = statement.getSubject();
                urisToBeVisited.push(subject.getURI());
            }

        } catch (Exception e) {
            System.out.println("Error while trying to get data from: " + resourceURI);
        } finally {
            while (!urisToBeVisited.isEmpty()) {
                String newUriToBeVisited = urisToBeVisited.pop();
                if (!visitedUris.contains(newUriToBeVisited)) {
                    search(outputModel, newUriToBeVisited);
                }
            }

        }
    }

    private void searchAnonymousNode(final Model outputModel, final Resource anonymousNode) {
        StmtIterator stmtIterator = anonymousNode.getModel().listStatements(anonymousNode, null, (RDFNode) null);
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            RDFNode object = statement.getObject();

            if (object.isAnon()) {
                searchAnonymousNode(outputModel, object.asResource());
            } else {
                outputModel.add(statement);
            }
        }

    }
}
