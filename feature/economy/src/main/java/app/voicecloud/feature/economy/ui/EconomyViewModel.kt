package app.voicecloud.feature.economy.ui
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import app.voicecloud.feature.economy.data.EconomyRepository; import app.voicecloud.feature.economy.model.*
import dagger.hilt.android.lifecycle.HiltViewModel; import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch; import javax.inject.Inject
@HiltViewModel class EconomyViewModel @Inject constructor(private val repo:EconomyRepository):ViewModel(){
 private val _state=MutableStateFlow(EconomyUiState()); val state=_state.asStateFlow()
 fun load(section:EconomySection){ viewModelScope.launch { _state.value=EconomyUiState(loading=true,title=section.label); runCatching{repo.load(section.name)}.onSuccess{_state.value=EconomyUiState(title=section.label,payload=it)}.onFailure{_state.value=EconomyUiState(title=section.label,error=it.message?:"VoiceCloud could not load this section.")} } }
 fun claimCheckIn()=mutate("Daily check-in claimed") { repo.claimCheckIn() }
 fun claimTask(id:String)=mutate("Task reward claimed") { repo.claimTask(id) }
 fun buyTicket(id:String)=mutate("Ticket purchase verified") { repo.buyTicket(id) }
 fun buyItem(id:String)=mutate("Store purchase completed") { repo.buyItem(id) }
 fun equip(id:String)=mutate("Item equipped") { repo.equip(id) }
 fun unequip(id:String)=mutate("Item unequipped") { repo.unequip(id) }
 fun validateCoinPurchase(productId:String,token:String,orderId:String?)=mutate("Google Play purchase verified by VoiceCloud") { repo.validateCoinPurchase(productId,token,orderId) }
 fun verifyVip(productId:String,token:String)=mutate("VIP purchase verified by VoiceCloud") { repo.verifyVip(productId,token) }
 private fun mutate(notice:String, block:suspend()->Any){ viewModelScope.launch{ _state.update{it.copy(busy=true,error=null,notice=null)}; runCatching{block()}.onSuccess{_state.update{it.copy(busy=false,notice=notice)}}.onFailure{e->_state.update{it.copy(busy=false,error=e.message?:"Request failed")}} } }
}
