# OEMInfo Test Application


This test application is a follow up of the original work done by Darryn Campbell with his DeviceIdentifiers Demo application: https://github.com/darryncampbell/OEMInfo-DeviceIdentifiers-Sample-Android

For those who are not aware of the original problem, starting with Android 10, Google restricted the way of accessing non-resettable identifiers which means SN, IMEI etc..
It is still possible to access those information in the original manner but only for applications which are treated as System ones and with a special permission which is not the case for the normal installable appplications.

It is possible to access these information though, through a custom content provider which Zebra has made available for the developers.

<b>If you plan to include in your application support to access these identifiers by using the EMDK library, please check out the source code of this application and the documented lines to understand how it works.</b>
<br></br>
<b>For anything else, please have a look at the original README document written by Darryn.</b>

## Key features of the application

- Written & Optimized entirely in Kotlin
- Fully automated & no need to stage the Device in order to access the required info from the CP
- User friendly also for the WS50

<img src="https://user-images.githubusercontent.com/6454841/187258485-e572b05a-30dd-4b6b-8567-5c7ff41056e2.png" align="left" width=40% height=40%>
<img src="https://user-images.githubusercontent.com/6454841/187258514-faa08f3c-66df-434b-8b1d-122bf59c7f65.png" align="right" width=35% height=35%>
