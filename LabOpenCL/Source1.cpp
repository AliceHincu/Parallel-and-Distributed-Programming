// This shows a simple usage of OpenCL
//modified
#include <utility>
#define CL_USE_DEPRECATED_OPENCL_2_0_APIS
#include <CL/cl.hpp>

#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include<vector>
#include <string>
#include <iterator>

using namespace std;

#define COEF 1024

void exitIfError(cl_int errorCode, char const* msg);
void errCallback(const char* errinfo, const void* private_info, size_t cb, void* user_data);

void printPoly(vector<cl_int>& a) {
	for (auto as : a) {
		cout << as << " ";
	}
	cout << '\n';
}

vector<int> increaseDegree(vector<int>& a, int deg) {
	vector<int> c;
	for (int i = 0; i < a.size(); i++) {
		c.push_back(a[i]);
	}
	for (int i = 0; i < deg; i++) {
		c.push_back(0);
	}

	return c;
}

vector<int> enlargePoly(vector<int>& a, int factor) {
	vector<int> c;
	for (int i = 0; i < factor; i++)
	{
		c.push_back(0);
	}
	for (int i = 0; i < a.size(); i++)
	{
		c.push_back(a[i]);
	}

	return c;
}

vector<int> subPoly(vector<int>& a, vector<int>& b, cl::CommandQueue& queue, cl::Context& context, cl::Program& program) {

	cl_int err;
	vector<cl_int> tmp(a.size());
	size_t cf = a.size();
	cl::Buffer inputBuf1(context, CL_MEM_READ_ONLY, cf * 4, nullptr, &err);
	exitIfError(err, "Buffer::Buffer1()");

	cl::Buffer inputBuf2(context, CL_MEM_READ_ONLY, cf * 4, nullptr, &err);
	exitIfError(err, "Buffer::Buffer2()");

	cl::Buffer outputBuf(context, CL_MEM_WRITE_ONLY, cf * 4, nullptr, &err);
	exitIfError(err, "Buffer::OutBuffer()");


	std::vector<cl::Event> afterCopyIn(2);
	std::vector<cl::Event> afterKernelExec(1);
	std::vector<cl::Event> afterCopyOut(1);

	cl::Kernel kernel(program, "sub", &err);
	exitIfError(err, "Kernel::CreateKernel()");

	int size = a.size();

	kernel.setArg(0, inputBuf1);
	kernel.setArg(1, inputBuf2);
	kernel.setArg(2, outputBuf);
	kernel.setArg(3, size);

	err = queue.enqueueWriteBuffer(inputBuf1, CL_FALSE, 0, 4 * a.size(), a.data(), nullptr, &afterCopyIn[0]);
	exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");

	err = queue.enqueueWriteBuffer(inputBuf2, CL_FALSE, 0, 4 * b.size(), b.data(), nullptr, &afterCopyIn[1]);
	exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");

	err = queue.enqueueNDRangeKernel(kernel,
		cl::NDRange(0),
		cl::NDRange(1),
		cl::NDRange(1),
		&afterCopyIn,
		&afterKernelExec[0]);
	exitIfError(err, "CommandQueue::enqueueNDRangeKernel()");

	afterKernelExec[0].wait();
	err = queue.enqueueReadBuffer(outputBuf, CL_TRUE, 0, 4 * tmp.size(), tmp.data(), nullptr, &afterCopyOut[0]);
	exitIfError(err, "ComamndQueue::enqueueReadBuffer()");
	afterCopyOut[0].wait();
	return tmp;
}

vector<cl_int> addPoly(vector<cl_int>& a, vector<cl_int>& b, cl::CommandQueue& queue, cl::Context& context, cl::Program& program) {

	cl_int err;
	vector<cl_int> tmp(a.size());
	size_t cf = a.size();
	cl::Buffer inputBuf1(context, CL_MEM_READ_ONLY, cf * 4, nullptr, &err);
	exitIfError(err, "Buffer::Buffer1()");

	cl::Buffer inputBuf2(context, CL_MEM_READ_ONLY, cf * 4, nullptr, &err);
	exitIfError(err, "Buffer::Buffer2()");

	cl::Buffer outputBuf(context, CL_MEM_WRITE_ONLY, cf * 4, nullptr, &err);
	exitIfError(err, "Buffer::OutBuffer()");


	std::vector<cl::Event> afterCopyIn(2);
	std::vector<cl::Event> afterKernelExec(1);
	std::vector<cl::Event> afterCopyOut(1);

	cl::Kernel kernel(program, "add", &err);
	exitIfError(err, "Kernel::CreateKernel()");

	int size = a.size();

	kernel.setArg(0, inputBuf1);
	kernel.setArg(1, inputBuf2);
	kernel.setArg(2, outputBuf);
	kernel.setArg(3, size);

	err = queue.enqueueWriteBuffer(inputBuf1, CL_FALSE, 0, 4 * a.size(), a.data(), nullptr, &afterCopyIn[0]);
	exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");

	err = queue.enqueueWriteBuffer(inputBuf2, CL_FALSE, 0, 4 * b.size(), b.data(), nullptr, &afterCopyIn[1]);
	exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");

	err = queue.enqueueNDRangeKernel(kernel,
		cl::NDRange(0),
		cl::NDRange(1),
		cl::NDRange(1),
		&afterCopyIn,
		&afterKernelExec[0]);
	exitIfError(err, "CommandQueue::enqueueNDRangeKernel()");

	afterKernelExec[0].wait();
	err = queue.enqueueReadBuffer(outputBuf, CL_TRUE, 0, 4 * tmp.size(), tmp.data(), nullptr, &afterCopyOut[0]);
	exitIfError(err, "ComamndQueue::enqueueReadBuffer()");
	afterCopyOut[0].wait();
	return tmp;
}

vector<int> computeResult(vector<int>& res1, vector<int>& res4, vector<int>& inter3, int coef, int newCoef, cl::CommandQueue& queue, cl::Context& context, cl::Program& program) {
	vector<int> inter4 = subPoly(inter3, res1,queue,context,program);
	vector<int> inter5 = subPoly(inter4, res4,queue,context,program);

	//(P1(X)+P2(X)) * (Q1(X)+Q2(X)) - P1(X)* Q1(X) - P2(X)*Q2(X)

	res1 = increaseDegree(res1, coef);
	res4 = enlargePoly(res4, coef);
	vector<int> res6 = enlargePoly(inter5, newCoef);
	res6 = increaseDegree(res6, newCoef);

	vector<int> res5 = addPoly(res1, res4,queue,context,program);
	vector<int> res7 = addPoly(res5, res6, queue, context, program);
	//P*Q=P0q0+p1q1+(p0+p1)(q0+q1)-p0q0-p1q1

	return res7;
}


vector<int> multiplyOneKara(vector<int>& a, vector<int>& b, int coef,cl::CommandQueue& queue,cl::Context& context,cl::Program& program) {


	if (coef == 1024) {
	
		cl_int err;
		vector<cl_int> tmp(2 * coef - 1);
		size_t cf = coef;
		cl::Buffer inputBuf1(context, CL_MEM_READ_ONLY, cf * 4, nullptr, &err);
		exitIfError(err, "Buffer::Buffer1()");

		cl::Buffer inputBuf2(context, CL_MEM_READ_ONLY,  cf * 4, nullptr, &err);
		exitIfError(err, "Buffer::Buffer2()");

		cl::Buffer outputBuf(context, CL_MEM_WRITE_ONLY, (2 * cf - 1) * 4, nullptr, &err);
		exitIfError(err, "Buffer::OutBuffer()");


		std::vector<cl::Event> afterCopyIn(2);
		std::vector<cl::Event> afterKernelExec(1);
		std::vector<cl::Event> afterCopyOut(1);

		cl::Kernel kernel(program,"multiply",&err);
		exitIfError(err, "Kernel::CreateKernel()");

		kernel.setArg(0, inputBuf1);
		kernel.setArg(1, inputBuf2);
		kernel.setArg(2, outputBuf);
		kernel.setArg(3, coef);

		err = queue.enqueueWriteBuffer(inputBuf1, CL_FALSE, 0, 4 * a.size(), a.data(), nullptr, &afterCopyIn[0]);
		exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");

		err = queue.enqueueWriteBuffer(inputBuf2, CL_FALSE, 0, 4 * b.size(), b.data(), nullptr, &afterCopyIn[1]);
		exitIfError(err, "ComamndQueue::enqueueWriteBuffer()");

		err = queue.enqueueNDRangeKernel(kernel,
			cl::NDRange(0),
			cl::NDRange(1),
			cl::NDRange(1),
			&afterCopyIn,
			&afterKernelExec[0]);
		exitIfError(err, "CommandQueue::enqueueNDRangeKernel()");


		afterKernelExec[0].wait();
		err = queue.enqueueReadBuffer(outputBuf, CL_TRUE, 0, 4 * tmp.size(), tmp.data(), nullptr, &afterCopyOut[0]);
		exitIfError(err, "ComamndQueue::enqueueReadBuffer()");
		afterCopyOut[0].wait();
		return tmp;
	}

	vector<int> a1, a2;
	vector<int> b1, b2;
	for (int i = 0; i < coef / 2; i++)
	{
		a1.push_back(a[i]);
		a2.push_back(a[i + coef / 2]);
		b1.push_back(b[i]);
		b2.push_back(a[i + coef / 2]);

	}
	int newCoef = coef / 2;

	vector<int> inter1 = addPoly(a1, a2, queue, context, program);
	vector<int> inter2 = addPoly(b1, b2, queue, context, program);

	vector<int> res1 = multiplyOneKara(a1, b1, newCoef,queue,context, program);
	vector<int> res4 = multiplyOneKara(a2, b2, newCoef,queue,context, program);
	vector<int> inter3 = multiplyOneKara(inter1, inter2, newCoef,queue,context, program);


	return computeResult(res1,res4,inter3,coef,newCoef,queue,context,program);
}


int main()
{
	cl_int err;
	std::vector<cl::Platform> platformList;
	err = cl::Platform::get(&platformList);
	exitIfError(platformList.empty() ? CL_PLATFORM_NOT_FOUND_KHR : err, "cl::Platform::get()");

	std::vector<cl::Device> deviceList;
	err = platformList[0].getDevices(CL_DEVICE_TYPE_GPU, &deviceList);
	exitIfError(deviceList.empty() ? CL_DEVICE_NOT_FOUND : err, "cl::Platform::getDevices()");

	cl::Context context(CL_DEVICE_TYPE_GPU, nullptr, nullptr/*&errCallback*/, nullptr, &err);
	exitIfError(err, "Conext::Context()");

	std::vector<cl::Device> const devices = context.getInfo<CL_CONTEXT_DEVICES>();
	exitIfError(devices.size() > 0 ? CL_SUCCESS : -1, "devices.size() > 0");
	std::cerr << "Found " << devices.size() << " device(s)\n";
	for (cl::Device const& device : devices) {
		std::cerr << "  type = " << device.getInfo<CL_DEVICE_NAME>() << "\n";
	}
	std::string prog(
		"#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable\n"
		"__kernel void multiply(__global int const* a,__global int const* b, __global int* out, int size) {\n"
		"	for(int i=0;i<size;i++){\n"
		"		for(int j=0;j<size;j++){\n"
		"			out[i+j]+=a[i]*b[j];\n"
		"		}\n"
		"	}\n"
		"}\n"
		"__kernel void add(__global int const* a,__global int const* b, __global int* out, int size) {\n"
		"	for(int i=0;i<size;i++){\n"
		"		out[i]=a[i]+b[i];\n"
		"		}\n"
		"}\n"
		"__kernel void sub(__global int const* a,__global int const* b, __global int* out, int size) {\n"
		"	for(int i=0;i<size;i++){\n"
		"		out[i]=a[i]-b[i];\n"
		"		}\n"
		"}\n"
	);
	cl::Program::Sources source(1, std::make_pair(prog.c_str(), prog.length() + 1));
	cl::Program program(context, source);
	err = program.build(devices, "");

	exitIfError(err, "program build()");

	cl::CommandQueue queue(context, devices[0], 0, &err);
	exitIfError(err, "CommandQueue::CommandQueue()");

	vector<cl_int> a, b, c;
	for (unsigned int i = 0; i < COEF; i++)
	{
		a.push_back(i + 1);
		b.push_back(i + 1);
	}


	c = multiplyOneKara(a, b,COEF, queue, context,program);
	printPoly(c);

	return EXIT_SUCCESS;
}

const char* getErrorString(cl_int error)
{
	switch (error) {
		// run-time and JIT compiler errors
	case CL_SUCCESS:
		return "CL_SUCCESS";
	case CL_DEVICE_NOT_FOUND:
		return "CL_DEVICE_NOT_FOUND";
	case CL_DEVICE_NOT_AVAILABLE:
		return "CL_DEVICE_NOT_AVAILABLE";
	case CL_COMPILER_NOT_AVAILABLE:
		return "CL_COMPILER_NOT_AVAILABLE";
	case CL_MEM_OBJECT_ALLOCATION_FAILURE:
		return "CL_MEM_OBJECT_ALLOCATION_FAILURE";
	case CL_OUT_OF_RESOURCES:
		return "CL_OUT_OF_RESOURCES";
	case CL_OUT_OF_HOST_MEMORY:
		return "CL_OUT_OF_HOST_MEMORY";
	case -7: return "CL_PROFILING_INFO_NOT_AVAILABLE";
	case -8: return "CL_MEM_COPY_OVERLAP";
	case -9: return "CL_IMAGE_FORMAT_MISMATCH";
	case -10: return "CL_IMAGE_FORMAT_NOT_SUPPORTED";
	case -11: return "CL_BUILD_PROGRAM_FAILURE";
	case -12: return "CL_MAP_FAILURE";
	case -13: return "CL_MISALIGNED_SUB_BUFFER_OFFSET";
	case -14: return "CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST";
	case -15: return "CL_COMPILE_PROGRAM_FAILURE";
	case -16: return "CL_LINKER_NOT_AVAILABLE";
	case -17: return "CL_LINK_PROGRAM_FAILURE";
	case -18: return "CL_DEVICE_PARTITION_FAILED";
	case -19: return "CL_KERNEL_ARG_INFO_NOT_AVAILABLE";

		// compile-time errors
	case -30: return "CL_INVALID_VALUE";
	case -31: return "CL_INVALID_DEVICE_TYPE";
	case -32: return "CL_INVALID_PLATFORM";
	case -33: return "CL_INVALID_DEVICE";
	case -34: return "CL_INVALID_CONTEXT";
	case -35: return "CL_INVALID_QUEUE_PROPERTIES";
	case -36: return "CL_INVALID_COMMAND_QUEUE";
	case -37: return "CL_INVALID_HOST_PTR";
	case -38: return "CL_INVALID_MEM_OBJECT";
	case -39: return "CL_INVALID_IMAGE_FORMAT_DESCRIPTOR";
	case -40: return "CL_INVALID_IMAGE_SIZE";
	case -41: return "CL_INVALID_SAMPLER";
	case -42: return "CL_INVALID_BINARY";
	case -43: return "CL_INVALID_BUILD_OPTIONS";
	case -44: return "CL_INVALID_PROGRAM";
	case -45: return "CL_INVALID_PROGRAM_EXECUTABLE";
	case -46: return "CL_INVALID_KERNEL_NAME";
	case -47: return "CL_INVALID_KERNEL_DEFINITION";
	case -48: return "CL_INVALID_KERNEL";
	case -49: return "CL_INVALID_ARG_INDEX";
	case -50: return "CL_INVALID_ARG_VALUE";
	case -51: return "CL_INVALID_ARG_SIZE";
	case -52: return "CL_INVALID_KERNEL_ARGS";
	case -53: return "CL_INVALID_WORK_DIMENSION";
	case -54: return "CL_INVALID_WORK_GROUP_SIZE";
	case -55: return "CL_INVALID_WORK_ITEM_SIZE";
	case -56: return "CL_INVALID_GLOBAL_OFFSET";
	case -57: return "CL_INVALID_EVENT_WAIT_LIST";
	case -58: return "CL_INVALID_EVENT";
	case -59: return "CL_INVALID_OPERATION";
	case -60: return "CL_INVALID_GL_OBJECT";
	case -61: return "CL_INVALID_BUFFER_SIZE";
	case -62: return "CL_INVALID_MIP_LEVEL";
	case -63: return "CL_INVALID_GLOBAL_WORK_SIZE";
	case -64: return "CL_INVALID_PROPERTY";
	case -65: return "CL_INVALID_IMAGE_DESCRIPTOR";
	case -66: return "CL_INVALID_COMPILER_OPTIONS";
	case -67: return "CL_INVALID_LINKER_OPTIONS";
	case -68: return "CL_INVALID_DEVICE_PARTITION_COUNT";

		// extension errors
	case -1000: return "CL_INVALID_GL_SHAREGROUP_REFERENCE_KHR";
	case -1001: return "CL_PLATFORM_NOT_FOUND_KHR";
	case -1002: return "CL_INVALID_D3D10_DEVICE_KHR";
	case -1003: return "CL_INVALID_D3D10_RESOURCE_KHR";
	case -1004: return "CL_D3D10_RESOURCE_ALREADY_ACQUIRED_KHR";
	case -1005: return "CL_D3D10_RESOURCE_NOT_ACQUIRED_KHR";
	default: return "Unknown OpenCL error";
	}
}

void exitIfError(cl_int errorCode, char const* msg)
{
	if (errorCode != CL_SUCCESS) {
		fprintf(stderr, "OpenCL error at %s: %s(%d)\n", msg, getErrorString(errorCode), errorCode);
		exit(1);
	}
}

void errCallback(const char* errinfo, const void* private_info, size_t cb, void* user_data)
{
	fprintf(stderr, "OpenCL error callback: %s\n", errinfo);
	exit(1);
}