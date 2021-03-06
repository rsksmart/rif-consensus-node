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
package org.hyperledger.besu.ethereum.merkleutils;

import org.hyperledger.besu.ethereum.chain.MutableBlockchain;
import org.hyperledger.besu.ethereum.core.MutableWorldState;
import org.hyperledger.besu.ethereum.proof.WorldStateProofProvider;
import org.hyperledger.besu.ethereum.worldstate.MarkSweepPruner;
import org.hyperledger.besu.ethereum.worldstate.WorldStatePreimageStorage;
import org.hyperledger.besu.ethereum.worldstate.WorldStateStorage;
import org.hyperledger.besu.metrics.ObservableMetricsSystem;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorage;

import org.apache.tuweni.bytes.Bytes32;

/**
 * Abstraction layer for creating instances of classes depending on different Merkle storage models.
 *
 * @author ppedemon
 */
public interface MerkleAwareProvider {

  /**
   * Create a new {@link MutableWorldState} instance.
   *
   * @param storage world state storage
   * @param preImageStorage world state pre-image storage
   * @return {@link MutableWorldState} instance
   */
  MutableWorldState createMutableWorldState(
      WorldStateStorage storage, WorldStatePreimageStorage preImageStorage);

  /**
   * Create a new {@link MutableWorldState} instance.
   *
   * @param rootHash root hash of the state represented by the instance to create
   * @param storage world state storage
   * @param preImageStorage world state pre-image storage
   * @return {@link MutableWorldState} instance
   */
  MutableWorldState createMutableWorldState(
      Bytes32 rootHash, WorldStateStorage storage, WorldStatePreimageStorage preImageStorage);

  /**
   * Create a new {@link WorldStateProofProvider}.
   *
   * @param storage storage for returned provider
   * @return {@link WorldStateProofProvider} instance
   */
  WorldStateProofProvider createWorldStateProofProvider(WorldStateStorage storage);

  /**
   * Create a new {@link MarkSweepPruner}.
   *
   * @param storage storage to prune
   * @param blockchain blockchain providing mark roots
   * @param pruningStorage storage used by the prunner to store marked nodes
   * @param metricsSystem metrics system keeping track of pruner statistics
   * @return {@link MarkSweepPruner} instance
   */
  MarkSweepPruner createMarkSweepPruner(
      WorldStateStorage storage,
      MutableBlockchain blockchain,
      KeyValueStorage pruningStorage,
      ObservableMetricsSystem metricsSystem);

  /**
   * Accept a {@link MerkleAwareProviderVisitor}.
   *
   * @param visitor visitor to accept
   * @param <T> type of result returned by visitor
   * @return result returned by visitor
   */
  <T> T accept(MerkleAwareProviderVisitor<T> visitor);
}
