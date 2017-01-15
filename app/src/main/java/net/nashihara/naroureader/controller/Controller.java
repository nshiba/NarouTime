package net.nashihara.naroureader.controller;

public interface Controller<V> {

    void attach(V view);

    void detach();
}
