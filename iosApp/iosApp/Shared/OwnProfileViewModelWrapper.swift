import Combine
import Foundation
import Shared

@MainActor
final class OwnProfileViewModelWrapper: ObservableObject {

    let viewModel: OwnProfileViewModel

    @Published var state: OwnProfileUiState

    init() {
        let vm = KoinIOSKt.getOwnProfileViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any OwnProfileEffect) -> Void) {
        Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
        Task {
            for await effect in viewModel.effect {
                onEffect(effect)
            }
        }
    }

    func load()           { viewModel.onEvent(event: OwnProfileEventLoad.shared) }
    func startEditName()  { viewModel.onEvent(event: OwnProfileEventStartEditName.shared) }
    func saveName()       { viewModel.onEvent(event: OwnProfileEventSaveName.shared) }
    func cancelEditName() { viewModel.onEvent(event: OwnProfileEventCancelEditName.shared) }
    func addPhone()       { viewModel.onEvent(event: OwnProfileEventAddPhoneTapped.shared) }
    func editCar()        { viewModel.onEvent(event: OwnProfileEventEditCarTapped.shared) }

    func nameDraftChanged(_ name: String) {
        viewModel.onEvent(event: OwnProfileEventNameDraftChanged(name: name))
    }

    func startEditMobilepay()  { viewModel.onEvent(event: OwnProfileEventStartEditMobilepay.shared) }
    func saveMobilepay()       { viewModel.onEvent(event: OwnProfileEventSaveMobilepay.shared) }
    func cancelEditMobilepay() { viewModel.onEvent(event: OwnProfileEventCancelEditMobilepay.shared) }

    func mobilepayDraftChanged(_ number: String) {
        viewModel.onEvent(event: OwnProfileEventMobilepayDraftChanged(number: number))
    }

    func uploadAvatar(data: Data) {
        let kotlinArray = KotlinByteArray(size: Int32(data.count))
        data.withUnsafeBytes { ptr in
            let bytes = ptr.bindMemory(to: UInt8.self)
            for (i, b) in bytes.enumerated() {
                kotlinArray.set(index: Int32(i), value: Int8(bitPattern: b))
            }
        }
        viewModel.onEvent(event: OwnProfileEventUploadAvatar(imageData: kotlinArray, contentType: "image/jpeg"))
    }
}
