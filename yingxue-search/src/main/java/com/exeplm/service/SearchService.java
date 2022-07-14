package com.exeplm.service;

import java.io.IOException;
import java.util.Map;

public interface SearchService {
    Map<String,Object> searchVideos(String q, Integer page, Integer per_page) throws IOException;
}
