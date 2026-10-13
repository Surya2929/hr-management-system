import { Outlet } from 'react-router-dom';
import Sidebar from '../../components/Sidebar';

export default function EmpLayout() {
  return (
    <div className="min-h-screen bg-slate-100 md:flex overflow-x-hidden">
      <Sidebar />
      <main className="w-full min-w-0 flex-1 md:ml-64 pt-20 md:pt-6 lg:pt-8 px-4 sm:px-5 md:px-6 lg:px-8 pb-4 sm:pb-5 md:pb-6 lg:pb-8 overflow-x-hidden">
        <div className="w-full max-w-[1600px] mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}