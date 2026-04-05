using System.Reflection;
using System.Runtime.Loader;

namespace Aerolinea.API.Helpers
{
    public class CustomAssemblyLoadContext : AssemblyLoadContext
    {
        public IntPtr LoadUnmanagedLibrary(string absolutePath)
        {
            return LoadUnmanagedDll(absolutePath);
        }

        protected override Assembly? Load(AssemblyName assemblyName)
        {
            return null;
        }
    }
}