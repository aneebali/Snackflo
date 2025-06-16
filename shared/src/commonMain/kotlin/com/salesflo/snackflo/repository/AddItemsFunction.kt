package com.salesflo.snackflo.repository

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.salesflo.snackflo.common.AppConstant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map


class RestaurantRepository {
    private val firestore = Firebase.firestore

    fun getRestaurants(): Flow<List<Restaurant>> {
        return firestore.collection(AppConstant.RESTAURANT)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data<Restaurant>()
                    } catch (e: Exception) {
                        println("Error parsing restaurant document ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
    }

    fun getItems(): Flow<List<Item>> {
        return firestore.collection(AppConstant.ITEMS_LIST)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val item = doc.data<Item>()
                        println(item)
                        println(doc.id)
                        item?.copy(id = doc.id)

                    } catch (e: Exception) {
                        println("Error parsing item document ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
    }

    fun getRestaurantsWithItems(): Flow<List<RestaurantWithItems>> {
        return combine(
            getRestaurants(),
            getItems()
        ) { restaurants, items ->
            restaurants.map { restaurant ->
                RestaurantWithItems(
                    restaurant = restaurant,
                    items = items.filter { it.categoryId == restaurant.id }
                )
            }.filter { it.restaurant.name.isNotEmpty() }
        }
    }
}

class RestaurantViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) {
    private val _uiState = mutableStateOf(RestaurantUiState())
    val uiState: State<RestaurantUiState> = _uiState

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

    }

    fun getRestaurantsWithItems(): Flow<List<RestaurantWithItems>> {
        return repository.getRestaurantsWithItems()
    }

    fun refreshData() {
        loadRestaurants()
    }
}

