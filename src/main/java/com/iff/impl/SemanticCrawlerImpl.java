package com.iff.impl;

import com.iff.crawler.SemanticCrawler;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.SelectorImpl;
import org.apache.jena.vocabulary.OWL;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
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
    public void search(Model outputModel, String resourceURI) {
        try {
            System.out.println("Searching URI: " + resourceURI);

            visitedUris.add(resourceURI);

            Model model = ModelFactory.createDefaultModel();
            // Step 1
            model.read(resourceURI);

            // Step 2
            StmtIterator stmtIterator = model.listStatements(ResourceFactory.createResource(resourceURI), null, (RDFNode) null);
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                RDFNode object = statement.getObject();
                if (object.isAnon()) {
                    searchAnonymousNode(outputModel, object.asResource());
                } else {
                    outputModel.add(statement);
                }
            }

            // Step 3: <URI> OWL.sameAs <algumValor> .
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
                if (subject.isAnon()) {
                    searchAnonymousNode(outputModel, subject);
                } else {
                    urisToBeVisited.push(subject.getURI());
                }
            }

        } catch (Exception e) {
            System.out.println("Error while trying to get data from: " + resourceURI);
        } finally {
            while (!urisToBeVisited.isEmpty()) {
                String newUriToBeVisited = urisToBeVisited.pop();
                if (shouldVisitUrl(newUriToBeVisited)) {
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

    private boolean shouldVisitUrl(String url) {
        if (visitedUris.contains(url)) {
            return false;
        }

        CharsetEncoder encoder = StandardCharsets.ISO_8859_1.newEncoder();
        return encoder.canEncode(url);
    }
}
