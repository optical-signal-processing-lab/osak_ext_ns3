build_lib(
  LIBNAME satellite
  SOURCE_FILES
    model/satellite-mobility-model.cc
    model/optical-device.cc
    model/optical-channel.cc
    helper/constellation-helper.cc
    helper/satellite-pcap-helper.cc
  HEADER_FILES
    model/satellite-mobility-model.h
    model/optical-device.h
    model/optical-channel.h
    helper/constellation-helper.h
    helper/satellite-pcap-helper.h
  LIBRARIES_TO_LINK
    ${libextension}
    ${libmobility}
    ${libpropagation}
    ${libnetwork}
)

build_exec(
      EXECNAME mobility-test
      SOURCE_FILES test/mobility-test.cc
      LIBRARIES_TO_LINK ${libcore} ${libsatellite} 
      EXECUTABLE_DIRECTORY_PATH ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/contrib/satellite/test/
    )

build_exec(
  EXECNAME constellation-test
  SOURCE_FILES test/constellation-test.cc
  LIBRARIES_TO_LINK ${libcore} ${libsatellite} 
  EXECUTABLE_DIRECTORY_PATH ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/contrib/satellite/test/
)

build_exec(
  EXECNAME satellite-pcap-test
  SOURCE_FILES test/satellite-pcap-test.cc
  LIBRARIES_TO_LINK ${libcore} ${libsatellite} ${libinternet} ${libapplications} 
  EXECUTABLE_DIRECTORY_PATH ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/contrib/satellite/test/
)