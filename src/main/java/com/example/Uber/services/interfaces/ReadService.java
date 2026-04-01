package com.example.Uber.services.interfaces;

import java.util.List;
import java.util.Optional;

public interface ReadService <Response,ID>{
    Optional<Response> findById(ID id);
    List<Response> findAll();
}
