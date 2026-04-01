package com.example.Uber.services.interfaces;

public interface WriteService<Request, Response, ID> {
    Response create (Request request);
    Response update(ID id, Request request);
    void deleteById(ID id);
}
