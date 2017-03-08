package net.nashihara.naroureader.presenter;

public interface Presenter<V> {

    void attach(V view);

    void detach();
}
