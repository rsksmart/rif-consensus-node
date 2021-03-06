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
 *
 */
package org.hyperledger.besu.ethereum.unitrie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyperledger.besu.ethereum.unitrie.ByteTestUtils.bytes;

import com.google.common.base.Strings;
import org.apache.tuweni.bytes.Bytes;
import org.junit.Test;

public class UniTrieHashingTest {

  private final UniNodeFactory nodeFactory = new DefaultUniNodeFactory();

  @Test
  public void equalityImpliesEqualHash() {
    byte[] value = Bytes.fromHexString("0x" + Strings.repeat("bad", 100)).toArrayUnsafe();

    UniNode trie0 =
        NullUniNode.instance()
            .accept(new PutVisitor(value, nodeFactory), Bytes.of(1, 0, 1))
            .accept(new PutVisitor(bytes(20), nodeFactory), Bytes.of(0, 1, 0));

    UniNode trie1 =
        NullUniNode.instance()
            .accept(new PutVisitor(value, nodeFactory), Bytes.of(1, 0, 1))
            .accept(new PutVisitor(bytes(20), nodeFactory), Bytes.of(0, 1, 0));

    assertThat(trie0.getHash()).isEqualTo(trie1.getHash());
  }

  @Test
  public void outOfOrder_equalityImpliesEqualHash() {
    byte[] value = Bytes.fromHexString("0x" + Strings.repeat("bad", 100)).toArrayUnsafe();

    UniNode trie0 =
        NullUniNode.instance()
            .accept(new PutVisitor(value, nodeFactory), Bytes.of(1, 0, 1))
            .accept(new PutVisitor(bytes(20), nodeFactory), Bytes.of(0, 1, 0));

    UniNode trie1 =
        NullUniNode.instance()
            .accept(new PutVisitor(bytes(20), nodeFactory), Bytes.of(0, 1, 0))
            .accept(new PutVisitor(value, nodeFactory), Bytes.of(1, 0, 1));

    assertThat(trie0.getHash()).isEqualTo(trie1.getHash());
  }

  @Test
  public void inequalityImpliesDifferentHash() {
    byte[] value = Bytes.fromHexString("0x" + Strings.repeat("bad", 100)).toArrayUnsafe();

    UniNode trie0 =
        NullUniNode.instance()
            .accept(new PutVisitor(value, nodeFactory), Bytes.of(1, 0, 1))
            .accept(new PutVisitor(bytes(20), nodeFactory), Bytes.of(0, 1, 0));

    UniNode trie1 =
        NullUniNode.instance()
            .accept(new PutVisitor(bytes(21), nodeFactory), Bytes.of(0, 1, 0))
            .accept(new PutVisitor(value, nodeFactory), Bytes.of(1, 0, 1));

    assertThat(trie0.getHash()).isNotEqualTo(trie1.getHash());
  }
}
