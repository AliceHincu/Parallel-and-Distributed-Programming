//// This shows a simple usage of OpenCL
//
//#include <utility>
//
//#include <CL/cl.hpp>
//
//#include <cstdio>
//#include <cstdlib>
//#include <fstream>
//#include <iostream>
//#include<vector>
//#include <string>
//#include <iterator>
//
//using namespace std;
//
//#define COEF 1024
//#define KERNELS 20
//
//void exitIfError(cl_int errorCode, char const* msg);
//void errCallback(const char* errinfo, const void* private_info, size_t cb, void* user_data);
//
//void printPoly(vector<cl_int>& a) {
//	for (auto as : a) {
//		cout << as << " ";
//	}
//	cout << '\n';
//}
//
//vector<cl_int> addPoly(vector<cl_int>& a, vector<cl_int>& b, cl::CommandQueue& queue, cl::Context& context, cl::Program& program) {
//
//	cl_int err;
//	vector<cl_int> tmp(a.size());
//	size_t cf = a.size();
//	cl::Buffer inputBuf1(context, CL_MEM_READ_ONLY, cf * 4, nullptr, &err);
//	exitIfError(err, "Buffer::Buffer1()");
//
//	cl::Buffer inputBuf2(context, CL_MEM_READ_ONLY, cf * 4, nullptr, &err);
//	exitIfError(err, "Buffer::Buffer2()");
//
//	cl::Buffer outputBuf(context, CL_MEM_WRITE_ONLY, cf * 4, nullptr, &err);
//	exitIfError(err, "Buffer::OutBuffer()");
//
//
//	std::vector<cl::Event> afterCopyIn(2);
//	std::vector<cl::Event> afterKernelExec(1);
//	std::vector<cl::Event> afterCopyOut(1);
//
//	cl::Kernel kernel(program, "add", &err);
//	exitIfError(err, "Kernel::CreateKernel()");
//
//	int size = a.size();
//
//	kernel.setArg(0, inputBuf1);
//	kernel.setArg(1, inputBuf2);
//	kernel.setArg(2, outputBuf);
//	kernel.setArg(3, size);
//
//	err = queue.enqueueWriteBuffer(inputBuf1, CL_FALSE, 0, 4 * a.size(), a.data(), nullptr, &afterCopyIn[0]);
//	exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");
//
//	err = queue.enqueueWriteBuffer(inputBuf2, CL_FALSE, 0, 4 * b.size(), b.data(), nullptr, &afterCopyIn[1]);
//	exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");
//
//	err = queue.enqueueNDRangeKernel(kernel,
//		cl::NDRange(0),
//		cl::NDRange(1),
//		cl::NDRange(1),
//		&afterCopyIn,
//		&afterKernelExec[0]);
//	exitIfError(err, "CommandQueue::enqueueNDRangeKernel()");
//
//	afterKernelExec[0].wait();
//	err = queue.enqueueReadBuffer(outputBuf, CL_TRUE, 0, 4 * tmp.size(), tmp.data(), nullptr, &afterCopyOut[0]);
//	exitIfError(err, "ComamndQueue::enqueueReadBuffer()");
//	afterCopyOut[0].wait();
//	return tmp;
//}
//
//
//
//int main()
//{
//	cl_int err;
//	std::vector<cl::Platform> platformList;
//	err = cl::Platform::get(&platformList);
//	exitIfError(platformList.empty() ? CL_PLATFORM_NOT_FOUND_KHR : err, "cl::Platform::get()");
//
//	std::vector<cl::Device> deviceList;
//	err = platformList[0].getDevices(CL_DEVICE_TYPE_GPU, &deviceList);
//	exitIfError(deviceList.empty() ? CL_DEVICE_NOT_FOUND : err, "cl::Platform::getDevices()");
//
//	cl::Context context(CL_DEVICE_TYPE_GPU, nullptr, nullptr/*&errCallback*/, nullptr, &err);
//	exitIfError(err, "Conext::Context()");
//
//	std::vector<cl::Device> const devices = context.getInfo<CL_CONTEXT_DEVICES>();
//	exitIfError(devices.size() > 0 ? CL_SUCCESS : -1, "devices.size() > 0");
//	std::cerr << "Found " << devices.size() << " device(s)\n";
//	for (cl::Device const& device : devices) {
//		std::cerr << "  type = " << device.getInfo<CL_DEVICE_NAME>() << "\n";
//	}
//	std::string prog(
//		"#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable\n"
//		"__kernel void multiply(__global int const* a,__global int const* b, __global int* out, int size, int begin, int end) {\n"
//		"	for(int i=begin;i<end;i++){\n"
//		"		for(int j=0;j<size;j++){\n"
//		"			out[i+j]+=a[i]*b[j];\n"
//		"		}\n"
//		"	}\n"
//		"}\n"
//		"__kernel void add(__global int const* a,__global int const* b, __global int* out, int size) {\n"
//		"	for(int i=0;i<size;i++){\n"
//		"		out[i]=a[i]+b[i];\n"
//		"		}\n"
//		"}\n"
//	);
//	cl::Program::Sources source(1, std::make_pair(prog.c_str(), prog.length() + 1));
//	cl::Program program(context, source);
//	err = program.build(devices, "");
//	exitIfError(err, "program build()");
//	vector<cl::Kernel> kernels;
//	for (size_t i = 0; i < KERNELS; i++)
//	{
//		kernels.emplace_back(program, "multiply", &err);
//	}
//	exitIfError(err, "Kernel::Kernel()");
//	std::cerr << "Kernels built\n";
//
//	cl::CommandQueue queue(context, devices[0], 0, &err);
//	exitIfError(err, "CommandQueue::CommandQueue()");
//
//	size_t coef = COEF;
//	int cf = coef;
//	cl::Buffer inputBuf1(context, CL_MEM_READ_ONLY, coef * 4, nullptr, &err);
//	exitIfError(err, "Buffer::Buffer1()");
//
//	cl::Buffer inputBuf2(context, CL_MEM_READ_ONLY, coef * 4, nullptr, &err);
//	exitIfError(err, "Buffer::Buffer2()");
//
//	cl::Buffer outputBuf(context, CL_MEM_WRITE_ONLY, (2*coef-1) * 4, nullptr, &err);
//	exitIfError(err, "Buffer::OutBuffer()");
//
//	int begin, end;
//
//	vector<cl_int> a, b,c(2*COEF-1);
//	vector<vector<cl_int>> temp;
//	vector<cl::Event> toWait(KERNELS);
//	for (int i = 0; i < KERNELS; i++) {
//		temp.push_back(vector<cl_int>(2 * COEF - 1));
//	}
//
//	for (unsigned int i = 0; i < COEF; i++)
//	{
//		a.push_back(i + 1);
//		b.push_back(i + 1);
//	}
//	
//	for (int i = 0; i < KERNELS; i++)
//	{
//		begin = (COEF * i) / KERNELS;
//		end = (COEF * (i+1)) / KERNELS;
//
//		vector<cl_int>& tmp = temp[i];
//		//cout << begin << '\n';
//		//cout << end << '\n';
//		cl::Kernel& kernel = kernels[i];
//		kernel.setArg(0, inputBuf1);
//		kernel.setArg(1, inputBuf2);
//		kernel.setArg(2, outputBuf);
//		kernel.setArg(3, cf);
//		kernel.setArg(4, begin);
//		kernel.setArg(5, end);
//
//		std::vector<cl::Event> afterCopyIn(2);
//		std::vector<cl::Event> afterKernelExec(1);
//		
//
//		err = queue.enqueueWriteBuffer(inputBuf1, CL_FALSE, 0, 4 * a.size(), a.data(), nullptr, &afterCopyIn[0]);
//		exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");
//
//		err = queue.enqueueWriteBuffer(inputBuf2, CL_FALSE, 0, 4 * b.size(), b.data(), nullptr, &afterCopyIn[1]);
//		exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");
//
//		err = queue.enqueueNDRangeKernel(kernel,
//			cl::NDRange(0),
//			cl::NDRange(1),
//			cl::NDRange(1),
//			&afterCopyIn,
//			&afterKernelExec[0]);
//		exitIfError(err, "CommandQueue::enqueueNDRangeKernel()");
//
//		afterKernelExec[0].wait();
//		err = queue.enqueueReadBuffer(outputBuf, CL_TRUE, 0, 4*tmp.size(), tmp.data(), nullptr, &toWait[i]);
//		exitIfError(err, "ComamndQueue::enqueueReadBuffer()");
//
//
//	}
//
//	for (size_t i = 0; i < KERNELS; i++)
//	{
//		toWait[i].wait();
//	}
//
//	for (size_t i = 0; i < KERNELS; i++)
//	{
//		c = addPoly(c, temp[i],queue,context,program);
//	}
//
//	printPoly(c);
//
//	return EXIT_SUCCESS;
//}
//
//const char* getErrorString(cl_int error)
//{
//	switch (error) {
//		// run-time and JIT compiler errors
//	case CL_SUCCESS:
//		return "CL_SUCCESS";
//	case CL_DEVICE_NOT_FOUND:
//		return "CL_DEVICE_NOT_FOUND";
//	case CL_DEVICE_NOT_AVAILABLE:
//		return "CL_DEVICE_NOT_AVAILABLE";
//	case CL_COMPILER_NOT_AVAILABLE:
//		return "CL_COMPILER_NOT_AVAILABLE";
//	case CL_MEM_OBJECT_ALLOCATION_FAILURE:
//		return "CL_MEM_OBJECT_ALLOCATION_FAILURE";
//	case CL_OUT_OF_RESOURCES:
//		return "CL_OUT_OF_RESOURCES";
//	case CL_OUT_OF_HOST_MEMORY:
//		return "CL_OUT_OF_HOST_MEMORY";
//	case -7: return "CL_PROFILING_INFO_NOT_AVAILABLE";
//	case -8: return "CL_MEM_COPY_OVERLAP";
//	case -9: return "CL_IMAGE_FORMAT_MISMATCH";
//	case -10: return "CL_IMAGE_FORMAT_NOT_SUPPORTED";
//	case -11: return "CL_BUILD_PROGRAM_FAILURE";
//	case -12: return "CL_MAP_FAILURE";
//	case -13: return "CL_MISALIGNED_SUB_BUFFER_OFFSET";
//	case -14: return "CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST";
//	case -15: return "CL_COMPILE_PROGRAM_FAILURE";
//	case -16: return "CL_LINKER_NOT_AVAILABLE";
//	case -17: return "CL_LINK_PROGRAM_FAILURE";
//	case -18: return "CL_DEVICE_PARTITION_FAILED";
//	case -19: return "CL_KERNEL_ARG_INFO_NOT_AVAILABLE";
//
//		// compile-time errors
//	case -30: return "CL_INVALID_VALUE";
//	case -31: return "CL_INVALID_DEVICE_TYPE";
//	case -32: return "CL_INVALID_PLATFORM";
//	case -33: return "CL_INVALID_DEVICE";
//	case -34: return "CL_INVALID_CONTEXT";
//	case -35: return "CL_INVALID_QUEUE_PROPERTIES";
//	case -36: return "CL_INVALID_COMMAND_QUEUE";
//	case -37: return "CL_INVALID_HOST_PTR";
//	case -38: return "CL_INVALID_MEM_OBJECT";
//	case -39: return "CL_INVALID_IMAGE_FORMAT_DESCRIPTOR";
//	case -40: return "CL_INVALID_IMAGE_SIZE";
//	case -41: return "CL_INVALID_SAMPLER";
//	case -42: return "CL_INVALID_BINARY";
//	case -43: return "CL_INVALID_BUILD_OPTIONS";
//	case -44: return "CL_INVALID_PROGRAM";
//	case -45: return "CL_INVALID_PROGRAM_EXECUTABLE";
//	case -46: return "CL_INVALID_KERNEL_NAME";
//	case -47: return "CL_INVALID_KERNEL_DEFINITION";
//	case -48: return "CL_INVALID_KERNEL";
//	case -49: return "CL_INVALID_ARG_INDEX";
//	case -50: return "CL_INVALID_ARG_VALUE";
//	case -51: return "CL_INVALID_ARG_SIZE";
//	case -52: return "CL_INVALID_KERNEL_ARGS";
//	case -53: return "CL_INVALID_WORK_DIMENSION";
//	case -54: return "CL_INVALID_WORK_GROUP_SIZE";
//	case -55: return "CL_INVALID_WORK_ITEM_SIZE";
//	case -56: return "CL_INVALID_GLOBAL_OFFSET";
//	case -57: return "CL_INVALID_EVENT_WAIT_LIST";
//	case -58: return "CL_INVALID_EVENT";
//	case -59: return "CL_INVALID_OPERATION";
//	case -60: return "CL_INVALID_GL_OBJECT";
//	case -61: return "CL_INVALID_BUFFER_SIZE";
//	case -62: return "CL_INVALID_MIP_LEVEL";
//	case -63: return "CL_INVALID_GLOBAL_WORK_SIZE";
//	case -64: return "CL_INVALID_PROPERTY";
//	case -65: return "CL_INVALID_IMAGE_DESCRIPTOR";
//	case -66: return "CL_INVALID_COMPILER_OPTIONS";
//	case -67: return "CL_INVALID_LINKER_OPTIONS";
//	case -68: return "CL_INVALID_DEVICE_PARTITION_COUNT";
//
//		// extension errors
//	case -1000: return "CL_INVALID_GL_SHAREGROUP_REFERENCE_KHR";
//	case -1001: return "CL_PLATFORM_NOT_FOUND_KHR";
//	case -1002: return "CL_INVALID_D3D10_DEVICE_KHR";
//	case -1003: return "CL_INVALID_D3D10_RESOURCE_KHR";
//	case -1004: return "CL_D3D10_RESOURCE_ALREADY_ACQUIRED_KHR";
//	case -1005: return "CL_D3D10_RESOURCE_NOT_ACQUIRED_KHR";
//	default: return "Unknown OpenCL error";
//	}
//}
//
//void exitIfError(cl_int errorCode, char const* msg)
//{
//	if (errorCode != CL_SUCCESS) {
//		fprintf(stderr, "OpenCL error at %s: %s(%d)\n", msg, getErrorString(errorCode), errorCode);
//		exit(1);
//	}
//}
//
//void errCallback(const char* errinfo, const void* private_info, size_t cb, void* user_data)
//{
//	fprintf(stderr, "OpenCL error callback: %s\n", errinfo);
//	exit(1);
//}