package net.nashihara.naroureader.presenter

interface Presenter<in V> {

    fun attach(view: V)

    fun detach()
}
