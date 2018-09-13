package com.me4502.cab432;

import com.me4502.cab432.app.PhotoApp;

import java.io.IOException;

public class Assignment1 {

    public static void main(String[] args) {
        try {
            PhotoApp.getInstance().load();
        } catch (IOException e) {
            // If an exception makes it here, runtime.
            throw new RuntimeException(e);
        }
    }
}
