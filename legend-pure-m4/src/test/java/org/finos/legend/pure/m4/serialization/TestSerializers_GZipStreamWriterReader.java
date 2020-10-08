// Copyright 2020 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.pure.m4.serialization;

import org.finos.legend.pure.m4.serialization.binary.BinaryReaders;
import org.finos.legend.pure.m4.serialization.binary.BinaryWriters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TestSerializers_GZipStreamWriterReader extends TestSerializers
{
    @Override
    protected WriterReader newWriterReader()
    {
        return new WriterReader()
        {
            private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

            @Override
            public Writer getWriter() throws IOException
            {
                return BinaryWriters.newBinaryWriter(new GZIPOutputStream(this.stream));
            }

            @Override
            public Reader getReader() throws IOException
            {
                return BinaryReaders.newBinaryReader(new GZIPInputStream(new ByteArrayInputStream(this.stream.toByteArray())));
            }
        };
    }
}
