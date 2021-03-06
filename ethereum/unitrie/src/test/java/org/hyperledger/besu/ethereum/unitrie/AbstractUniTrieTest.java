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

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.ethereum.trie.KeyValueMerkleStorage;
import org.hyperledger.besu.ethereum.trie.MerkleStorage;
import org.hyperledger.besu.ethereum.trie.Proof;
import org.hyperledger.besu.services.kvstore.InMemoryKeyValueStorage;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Strings;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractUniTrieTest {

  UniTrie<Bytes, String> trie;

  abstract DataLoader loader();

  abstract UniTrie<Bytes, String> createTrie();

  @Before
  public void setup() {
    trie = createTrie();
  }

  @Test
  public void emptyTreeReturnsEmpty() {
    assertFalse(trie.get(Bytes.EMPTY).isPresent());
  }

  @Test
  public void emptyTreeHasKnownRootHash() {
    assertThat(trie.getRootHash().toString()).isEqualTo(UniTrie.NULL_UNINODE_HASH.toString());
  }

  @Test(expected = NullPointerException.class)
  public void throwsOnUpdateWithNull() {
    trie.put(Bytes.EMPTY, null);
  }

  @Test
  public void replaceSingleValue() {
    final Bytes key = Bytes.of(1);
    final String value1 = "value1";
    trie.put(key, value1);
    assertThat(trie.get(key)).isEqualTo(Optional.of(value1));

    final String value2 = "value2";
    trie.put(key, value2);
    assertThat(trie.get(key)).isEqualTo(Optional.of(value2));
  }

  @Test
  public void hashChangesWhenSingleValueReplaced() {
    final Bytes key = Bytes.of(1);
    final String value1 = "value1";
    trie.put(key, value1);
    final Bytes32 hash1 = trie.getRootHash();

    final String value2 = "value2";
    trie.put(key, value2);
    final Bytes32 hash2 = trie.getRootHash();

    assertThat(hash1).isNotEqualTo(hash2);

    trie.put(key, value1);
    assertThat(trie.getRootHash()).isEqualTo(hash1);
  }

  @Test
  public void readPastLeaf() {
    final Bytes key1 = Bytes.of(1);
    trie.put(key1, "value");
    final Bytes key2 = Bytes.of(16);
    assertFalse(trie.get(key2).isPresent());
  }

  @Test
  public void branchValue() {
    final Bytes key1 = Bytes.of(1);
    final Bytes key2 = Bytes.of(1, 1);

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    assertThat(trie.get(key1)).isEqualTo(Optional.of(value1));
    assertThat(trie.get(key2)).isEqualTo(Optional.of(value2));
  }

  @Test
  public void readPastBranch() {
    final Bytes key1 = Bytes.of(12);
    final Bytes key2 = Bytes.of(12, 54);

    final String value1 = "value1";
    trie.put(key1, value1);
    final String value2 = "value2";
    trie.put(key2, value2);

    final Bytes key3 = Bytes.of(3);
    assertFalse(trie.get(key3).isPresent());
  }

  @Test
  public void branchWithValue() {
    final Bytes key1 = Bytes.of(5);
    final Bytes key2 = Bytes.EMPTY;

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    assertThat(trie.get(key1)).isEqualTo(Optional.of(value1));
    assertThat(trie.get(key2)).isEqualTo(Optional.of(value2));
  }

  @Test
  public void childOfBranch() {
    final Bytes key1 = Bytes.of(1, 5, 9);
    final Bytes key2 = Bytes.of(1, 5, 2);

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    assertThat(trie.get(key1)).isEqualTo(Optional.of(value1));
    assertThat(trie.get(key2)).isEqualTo(Optional.of(value2));
    assertFalse(trie.get(Bytes.of(1, 4)).isPresent());
  }

  @Test
  public void branchOnTop() {
    final Bytes key1 = Bytes.of(0xfe, 1);
    final Bytes key2 = Bytes.of(0xfe, 2);
    final Bytes key3 = Bytes.of(0xe1, 1);

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    final String value3 = "value3";
    trie.put(key3, value3);

    assertThat(trie.get(key1)).isEqualTo(Optional.of(value1));
    assertThat(trie.get(key2)).isEqualTo(Optional.of(value2));
    assertThat(trie.get(key3)).isEqualTo(Optional.of(value3));
    assertFalse(trie.get(Bytes.of(1, 4)).isPresent());
    assertFalse(trie.get(Bytes.of(2, 4)).isPresent());
    assertFalse(trie.get(Bytes.of(3)).isPresent());
  }

  @Test
  public void splitBranches() {
    final Bytes key1 = Bytes.of(1, 5, 9);
    final Bytes key2 = Bytes.of(1, 5, 2);

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    final Bytes key3 = Bytes.of(1, 9, 1);

    final String value3 = "value3";
    trie.put(key3, value3);

    assertThat(trie.get(key1)).isEqualTo(Optional.of(value1));
    assertThat(trie.get(key2)).isEqualTo(Optional.of(value2));
    assertThat(trie.get(key3)).isEqualTo(Optional.of(value3));
  }

  @Test
  public void replaceBranchChild() {
    final Bytes key1 = Bytes.of(0);
    final Bytes key2 = Bytes.of(1);

    final String value1 = "value1";
    trie.put(key1, value1);
    final String value2 = "value2";
    trie.put(key2, value2);

    assertThat(trie.get(key1)).isEqualTo(Optional.of(value1));
    assertThat(trie.get(key2)).isEqualTo(Optional.of(value2));

    final String value3 = "value3";
    trie.put(key1, value3);

    assertThat(trie.get(key1)).isEqualTo(Optional.of(value3));
    assertThat(trie.get(key2)).isEqualTo(Optional.of(value2));
  }

  @Test
  public void forceCoalesce() {
    final Bytes key1 = Bytes.of(0);
    final Bytes key2 = Bytes.of(1);
    final Bytes key3 = Bytes.of(2);
    final Bytes key4 = Bytes.of(0, 0);
    final Bytes key5 = Bytes.of(0, 1);

    trie.put(key1, "value1");
    trie.put(key2, "value2");
    trie.put(key3, "value3");
    trie.put(key4, "value4");
    trie.put(key5, "value5");

    trie.remove(key2);
    trie.remove(key3);

    assertThat(trie.get(key1)).isEqualTo(Optional.of("value1"));
    assertFalse(trie.get(key2).isPresent());
    assertFalse(trie.get(key3).isPresent());
    assertThat(trie.get(key4)).isEqualTo(Optional.of("value4"));
    assertThat(trie.get(key5)).isEqualTo(Optional.of("value5"));
  }

  @Test
  public void removeUnexistingNodeHasNoEffect() {
    final Bytes key1 = Bytes.of(1, 5, 9);
    final Bytes key2 = Bytes.of(1, 5, 2);

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    final Bytes hash = trie.getRootHash();

    trie.remove(Bytes.of(1, 4));
    assertThat(trie.getRootHash()).isEqualTo(hash);
  }

  @Test
  public void hashChangesWhenValueChanged() {
    final Bytes key1 = Bytes.of(1, 5, 8, 9);
    final Bytes key2 = Bytes.of(1, 6, 1, 2);
    final Bytes key3 = Bytes.of(1, 6, 1, 3);

    final String value1 = "value1";
    trie.put(key1, value1);
    final Bytes32 hash1 = trie.getRootHash();

    final String value2 = "value2";
    trie.put(key2, value2);
    final String value3 = "value3";
    trie.put(key3, value3);
    final Bytes32 hash2 = trie.getRootHash();

    assertThat(hash1).isNotEqualTo(hash2);

    final String value4 = "value4";
    trie.put(key1, value4);
    final Bytes32 hash3 = trie.getRootHash();

    assertThat(hash1).isNotEqualTo(hash3);
    assertThat(hash2).isNotEqualTo(hash3);

    trie.put(key1, value1);
    assertThat(trie.getRootHash()).isEqualTo(hash2);

    trie.remove(key2);
    trie.remove(key3);
    assertThat(trie.getRootHash()).isEqualTo(hash1);
  }

  @Test
  public void getValueWithProof_emptyTrie() {
    final Bytes key1 = Bytes.of(0xfe, 1);

    Proof<String> valueWithProof = trie.getValueWithProof(key1);
    assertThat(valueWithProof.getValue()).isEmpty();
    assertThat(valueWithProof.getProofRelatedNodes()).hasSize(0);
  }

  @Test
  public void getValueWithProof_forExistingValues() {
    final Bytes key1 = Bytes.of(0xfe, 1);
    final Bytes key2 = Bytes.of(0xfe, 2);
    final Bytes key3 = Bytes.of(0xfe, 3);

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    final String value3 = "value3";
    trie.put(key3, value3);

    final Proof<String> valueWithProof = trie.getValueWithProof(key2);
    assertThat(valueWithProof.getProofRelatedNodes()).hasSize(2);
    assertThat(valueWithProof.getValue()).contains(value2);

    MerkleStorage storage = new KeyValueMerkleStorage(new InMemoryKeyValueStorage());
    List<UniNode> nodes =
        new UniTrieNodeDecoder(storage::get)
            .decodeNodes(valueWithProof.getProofRelatedNodes().get(1));

    assertThat(new String(nodes.get(1).getValue(loader()).get(), StandardCharsets.UTF_8))
        .isEqualTo(value2);
    assertThat(new String(nodes.get(2).getValue(loader()).get(), StandardCharsets.UTF_8))
        .isEqualTo(value3);
  }

  @Test
  public void getValueWithProof_forNonExistentValue() {
    final Bytes key1 = Bytes.of(0xfe, 1);
    final Bytes key2 = Bytes.of(0xfe, 2);
    final Bytes key3 = Bytes.of(0xfe, 3);
    final Bytes key4 = Bytes.of(0xfe, 4);

    final String value1 = "value1";
    trie.put(key1, value1);

    final String value2 = "value2";
    trie.put(key2, value2);

    final String value3 = "value3";
    trie.put(key3, value3);

    final Proof<String> valueWithProof = trie.getValueWithProof(key4);
    assertThat(valueWithProof.getValue()).isEmpty();
    assertThat(valueWithProof.getProofRelatedNodes()).hasSize(1);
  }

  @Test
  public void getValueWithProof_singleNodeTrie() {
    final Bytes key1 = Bytes.of(0xfe, 1);
    final String value1 = "1";
    trie.put(key1, value1);

    final Proof<String> valueWithProof = trie.getValueWithProof(key1);
    assertThat(valueWithProof.getValue()).contains(value1);
    assertThat(valueWithProof.getProofRelatedNodes()).hasSize(1);

    MerkleStorage storage = new KeyValueMerkleStorage(new InMemoryKeyValueStorage());
    List<UniNode> nodes =
        new UniTrieNodeDecoder(storage::get)
            .decodeNodes(valueWithProof.getProofRelatedNodes().get(0));

    assertThat(nodes.size()).isEqualTo(1);

    final String nodeValue =
        new String(nodes.get(0).getValue(loader()).get(), StandardCharsets.UTF_8);
    assertThat(nodeValue).isEqualTo(value1);
  }

  @Test
  public void getValueLength_nonExistentValue() {
    final Bytes key1 = Bytes.of(1, 5, 9);
    final String value1 = "value1";
    trie.put(key1, value1);

    final Bytes key2 = Bytes.of(1, 5, 2);
    final String value2 = "value2";
    trie.put(key2, value2);

    final Bytes key3 = Bytes.of(1, 9, 1);
    final String value3 = "value3";
    trie.put(key3, value3);

    assertThat(trie.getValueLength(Bytes.of(1, 2, 3, 4, 5, 5, 6))).isEmpty();
  }

  @Test
  public void getValueLength() {
    final Bytes key1 = Bytes.of(1, 5, 9);
    final String value1 = "value1";
    trie.put(key1, value1);

    final Bytes key2 = Bytes.of(1, 5, 2);
    final String value2 = Strings.repeat("x", 1021);
    trie.put(key2, value2);

    final Bytes key3 = Bytes.of(1, 9, 1);
    final String value3 = "value3";
    trie.put(key3, value3);

    assertThat(trie.getValueLength(key2)).contains(1021);
  }
}
