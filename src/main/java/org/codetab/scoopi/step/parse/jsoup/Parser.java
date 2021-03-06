package org.codetab.scoopi.step.parse.jsoup;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.step.base.BaseParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser extends BaseParser {

    static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    @Inject
    private ValueParser jsoupValueParser;
    @Inject
    private DocumentHelper documentHelper;

    @Override
    protected boolean postInitialize() {
        try {
            notNull(document, "document must not be null");
            validState(nonNull(document.getDocumentObject()),
                    "documentObject is not loaded");

            InputStream html = getDocumentHTML();
            Document page = Jsoup.parse(html, null, "");

            jsoupValueParser.setPage(page);
            setValueParser(jsoupValueParser);
            return true;
        } catch (DataFormatException | IOException | IllegalStateException
                | NullPointerException e) {
            String message = "unable to initialize parser";
            throw new StepRunException(message, e);
        }
    }

    private InputStream getDocumentHTML()
            throws DataFormatException, IOException {
        byte[] bytes = documentHelper.getDocumentObject(document);
        return new ByteArrayInputStream(bytes);
    }
}
