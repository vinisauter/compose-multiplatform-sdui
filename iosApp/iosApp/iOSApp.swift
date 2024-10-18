import SwiftUI

@main
struct iOSApp: App {
	var body: some Scene {
//        FirebaseApp.configure()
 	    MainControllerKt.initialise()
		WindowGroup {
			ContentView()
		}
	}
}