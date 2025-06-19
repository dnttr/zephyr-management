package org.dnttr.zephyr.management.config.files;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IFile<R> {

    public R load(@NotNull String path) throws IOException;
}
