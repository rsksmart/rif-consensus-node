/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.unitrie;

import org.hyperledger.besu.ethereum.unitrie.ints.UInt24;
import org.hyperledger.besu.ethereum.unitrie.ints.VarInt;
import org.hyperledger.besu.util.bytes.Bytes32;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.util.Optional;

/**
 * Interface for nodes in a Unitrie. A node has a path and an optional value.
 *
 * @author ppedemon
 */
public interface UniNode {

    /**
     * Get path for this node.
     *
     * @return path for this node
     */
    BytesValue getPath();

    /**
     * Get the wrapper for this value.
     *
     * @return   value wrapper for this node
     */
    ValueWrapper getValueWrapper();

    /**
     * Get optional value for this node.
     *
     * @return  optional holding node's value
     */
    Optional<BytesValue> getValue();

    /**
     * If value is present return its hash.
     *
     * @return  optional holding value's hash if present
     */
    Optional<Bytes32> getValueHash();

    /**
     * If value is present return its length in bytes.
     *
     * @return  optional holding value's length in bytes if present
     */
    Optional<UInt24> getValueLength();

    /**
     * Get left hand side child of this node.
     *
     * @return  left hand side child of this node
     */
    UniNode getLeftChild();

    /**
     * Get right hand side child of this node.
     *
     * @return  right hand side child of this node
     */
    UniNode getRightChild();

    /**
     * Get total size of children of this node in bytes.
     *
     * @return total size of children in bytes
     */
    VarInt getChildrenSize();

    /**
     * Get intrinsic size of this node, in bytes. Intrinsic size is defined as the
     * value size if it's long, plus children size, plus length of node's encoding.
     *
     * @return  intrinsic size of this node.
     */
    long intrinsicSize();

    /**
     * Pretty print a Uninode.
     *
     * @param indent  indentation to prepend
     * @return  string representation of a Uninode
     */
    String print(final int indent);

    /**
     * Accept a {@link UniPathVisitor}.
     *
     * @param visitor  visitor instance
     * @param path  path leading to this node
     * @return  node resulting form visit
     */
    UniNode accept(UniPathVisitor visitor, BytesValue path);

    /**
     * Aceept the given {@link UniNodeVisitor}.
     *
     * @param visitor  visitor instance
     */
    void accept(UniNodeVisitor visitor);

    /**
     * Get node encoding.
     *
     * @return  node encoding as dictated by RSKIP107
     */
    BytesValue getEncoding();

    /**
     * Get node hash. This will amount to the hash of the node's encoding
     *
     * @return  node hash, given by the hash of the node's encoding
     */
    Bytes32 getHash();

    /**
     * Whether this node will be referenced by hash or inlined. This is determined
     * by whether the encoding length exceeds some upper bound.
     *
     * @return  whether this node will be referenced by hash or inlined
     */
    boolean isReferencedByHash();

    /**
     * Whether this node needs to be persisted.
     *
     * @return  true iif this node needs to be persisted
     */
    boolean isDirty();

    /**
     * Mark this node as requiring to be persisted.
     */
    void markDirty();

    /**
     * Unloads this node. Action required only for nodes backed up by storage.
     */
    default void unload() {
    }
}
