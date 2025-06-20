package org.dnttr.zephyr.management.config;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IFile<R> {

    R load(@NotNull String path) throws IOException;
}
