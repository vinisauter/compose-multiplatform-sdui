import SwiftUI

@main
struct iOSApp: App {

    init() {
        MainControllerKt.initialise()
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}