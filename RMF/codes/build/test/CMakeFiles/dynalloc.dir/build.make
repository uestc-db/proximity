# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 2.8

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list

# Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/libs/eigen-eigen

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build

# Include any dependencies generated for this target.
include test/CMakeFiles/dynalloc.dir/depend.make

# Include the progress variables for this target.
include test/CMakeFiles/dynalloc.dir/progress.make

# Include the compile flags for this target's objects.
include test/CMakeFiles/dynalloc.dir/flags.make

test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o: test/CMakeFiles/dynalloc.dir/flags.make
test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o: /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/libs/eigen-eigen/test/dynalloc.cpp
	$(CMAKE_COMMAND) -E cmake_progress_report /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building CXX object test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o"
	cd /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/test && /usr/bin/c++   $(CXX_DEFINES) $(CXX_FLAGS) -o CMakeFiles/dynalloc.dir/dynalloc.cpp.o -c /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/libs/eigen-eigen/test/dynalloc.cpp

test/CMakeFiles/dynalloc.dir/dynalloc.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/dynalloc.dir/dynalloc.cpp.i"
	cd /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/test && /usr/bin/c++  $(CXX_DEFINES) $(CXX_FLAGS) -E /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/libs/eigen-eigen/test/dynalloc.cpp > CMakeFiles/dynalloc.dir/dynalloc.cpp.i

test/CMakeFiles/dynalloc.dir/dynalloc.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/dynalloc.dir/dynalloc.cpp.s"
	cd /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/test && /usr/bin/c++  $(CXX_DEFINES) $(CXX_FLAGS) -S /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/libs/eigen-eigen/test/dynalloc.cpp -o CMakeFiles/dynalloc.dir/dynalloc.cpp.s

test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.requires:
.PHONY : test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.requires

test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.provides: test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.requires
	$(MAKE) -f test/CMakeFiles/dynalloc.dir/build.make test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.provides.build
.PHONY : test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.provides

test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.provides.build: test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o

# Object files for target dynalloc
dynalloc_OBJECTS = \
"CMakeFiles/dynalloc.dir/dynalloc.cpp.o"

# External object files for target dynalloc
dynalloc_EXTERNAL_OBJECTS =

test/dynalloc: test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o
test/dynalloc: test/CMakeFiles/dynalloc.dir/build.make
test/dynalloc: test/CMakeFiles/dynalloc.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking CXX executable dynalloc"
	cd /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/test && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/dynalloc.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
test/CMakeFiles/dynalloc.dir/build: test/dynalloc
.PHONY : test/CMakeFiles/dynalloc.dir/build

test/CMakeFiles/dynalloc.dir/requires: test/CMakeFiles/dynalloc.dir/dynalloc.cpp.o.requires
.PHONY : test/CMakeFiles/dynalloc.dir/requires

test/CMakeFiles/dynalloc.dir/clean:
	cd /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/test && $(CMAKE_COMMAND) -P CMakeFiles/dynalloc.dir/cmake_clean.cmake
.PHONY : test/CMakeFiles/dynalloc.dir/clean

test/CMakeFiles/dynalloc.dir/depend:
	cd /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/libs/eigen-eigen /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/libs/eigen-eigen/test /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/test /mnt/disk_root_ori/home/xuying/proxim/6_pred_models/singapore/rmf/build/test/CMakeFiles/dynalloc.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : test/CMakeFiles/dynalloc.dir/depend

