/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.tar;

import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.ArchiveEntryIterator;
import com.mucommander.file.PermissionBits;
import com.mucommander.file.SimpleFilePermissions;
import com.mucommander.file.impl.tar.provider.TarEntry;
import com.mucommander.file.impl.tar.provider.TarInputStream;

import java.io.IOException;

/**
 * An <code>ArchiveEntryIterator</code> that iterates through a {@link TarInputStream}.
 *
 * @author Maxence Bernard
 */
class TarEntryIterator implements ArchiveEntryIterator {

    /** InputStream to the archive file */
    private TarInputStream tin;

    /** The next entry to be returned by #nextEntry(), null if there is no more entry */
    private ArchiveEntry nextEntry;

    /** The current entry, where the TarInputStream is currently positionned */
    private ArchiveEntry currentEntry;


    /**
     * Creates a new TarEntryIterator that iterates through the entries of the given {@link TarInputStream}.
     *
     * @param tin the TarInputStream to iterate through
     * @throws IOException if an error occurred while fetching the first entry
     */
    TarEntryIterator(TarInputStream tin) throws IOException {
        this.tin = tin;

        // Prefetch the first entry
        nextEntry = getNextEntry();
    }

    /**
     * Returns the {@link TarInputStream} instance that was used to create this object.
     *
     * @return the {@link TarInputStream} instance that was used to create this object.
     */
    TarInputStream getTarInputStream() {
        return tin;
    }

    /**
     * Returns the current entry, where the <code>TarInputStream</code> is currently positionned.
     *
     * @return the current entry, where the <code>TarInputStream</code> is currently positionned.
     */
    ArchiveEntry getCurrentEntry() {
        return currentEntry;
    }

    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given
     * <code>org.apache.tools.tar.TarEntry</code>.
     *
     * @param tarEntry the object that serves to initialize the attributes of the returned ArchiveEntry
     * @return an ArchiveEntry whose attributes are fetched from the given org.apache.tools.tar.TarEntry
     */
    private ArchiveEntry createArchiveEntry(TarEntry tarEntry) {
        ArchiveEntry entry = new ArchiveEntry(tarEntry.getName(), tarEntry.isDirectory(), tarEntry.getModTime().getTime(), tarEntry.getSize());
        entry.setPermissions(new SimpleFilePermissions(tarEntry.getMode() & PermissionBits.FULL_PERMISSION_INT));
        entry.setOwner(tarEntry.getUserName());
        entry.setGroup(tarEntry.getGroupName());
        entry.setEntryObject(tarEntry);

        return entry;
    }

    /**
     * Advances the {@link TarInputStream} to the next entry and returns the corresponding {@link ArchiveEntry}.
     *
     * @return the next ArchiveEntry
     * @throws IOException if an I/O error occurred
     */
    private ArchiveEntry getNextEntry() throws IOException {
        TarEntry entry = tin.getNextEntry();

        if(entry==null)
            return null;

        return createArchiveEntry(entry);
    }


    /////////////////////////////////////////
    // ArchiveEntryIterator implementation //
    /////////////////////////////////////////

    public boolean hasNextEntry() throws IOException {
        return nextEntry!=null;
    }

    public ArchiveEntry nextEntry() throws IOException {
        if(nextEntry==null)
            return null;

        this.currentEntry = nextEntry;

        // Get the next entry, if any
        nextEntry = getNextEntry();

        return currentEntry;
    }

    public void close() throws IOException {
        tin.close();
    }
}