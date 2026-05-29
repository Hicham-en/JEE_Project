package com.annotation.infrastructure.parser;

import com.annotation.application.dto.TextPairDTO;
import java.io.InputStream;
import java.util.List;

/**
 * Interface for Dataset parsers.
 */
public interface IDatasetParser {
    /**
     * Parses the given InputStream into a list of TextPairDTOs.
     *
     * @param inputStream the input stream to parse
     * @return list of extracted text pairs
     * @throws Exception if parsing fails
     */
    List<TextPairDTO> parse(InputStream inputStream) throws Exception;
}
