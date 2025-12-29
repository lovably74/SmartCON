import { Toaster } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import NotFound from "@/pages/NotFound";
import { Route, Switch } from "wouter";
import ErrorBoundary from "./components/ErrorBoundary";
import { ThemeProvider } from "./contexts/ThemeContext";

// Pages
import Intro from "./pages/Intro";
import LoginSelect from "./pages/auth/LoginSelect";
import LoginHQ from "./pages/auth/LoginHQ";
import RoleSelect from "./pages/auth/RoleSelect";
import Subscribe from "./pages/Subscribe";
import DashboardHQ from "./pages/hq/DashboardHQ";
import SitesHQ from "./pages/hq/SitesHQ";
import WorkersHQ from "./pages/hq/WorkersHQ";
import ContractsHQ from "./pages/hq/ContractsHQ";
import SettlementsHQ from "./pages/hq/SettlementsHQ";
import SettingsHQ from "./pages/hq/SettingsHQ";
import DashboardSite from "./pages/site/DashboardSite";
import AttendanceSite from "./pages/site/AttendanceSite";
import ReportsSite from "./pages/site/ReportsSite";
import ContractsSite from "./pages/site/ContractsSite";
import TeamsSite from "./pages/site/TeamsSite";
import SalarySite from "./pages/site/SalarySite";
import DashboardWorker from "./pages/worker/DashboardWorker";
import AttendanceWorker from "./pages/worker/AttendanceWorker";
import ContractsWorker from "./pages/worker/ContractsWorker";
import ProfileWorker from "./pages/worker/ProfileWorker";
import DashboardSuper from "./pages/super/DashboardSuper";
import TenantsSuper from "./pages/super/TenantsSuper";
import ApprovalsSuper from "./pages/super/ApprovalsSuper";
import BillingSuper from "./pages/super/BillingSuper";
import TaxSuper from "./pages/super/TaxSuper";
import SettingsSuper from "./pages/super/SettingsSuper";

function Router() {
  return (
    <Switch>
      {/* Public Routes */}
      <Route path="/" component={Intro} />
      <Route path="/login" component={LoginSelect} />
      <Route path="/login/hq" component={LoginHQ} />
      <Route path="/role-select" component={RoleSelect} />
      <Route path="/subscribe" component={Subscribe} />
      
      {/* HQ Routes */}
      <Route path="/hq/dashboard" component={DashboardHQ} />
      <Route path="/hq/sites" component={SitesHQ} />
      <Route path="/hq/workers" component={WorkersHQ} />
      <Route path="/hq/contracts" component={ContractsHQ} />
      <Route path="/hq/settlements" component={SettlementsHQ} />
      <Route path="/hq/settings" component={SettingsHQ} />

      
      {/* Site Routes */}
      <Route path="/site/dashboard" component={DashboardSite} />
      <Route path="/site/attendance" component={AttendanceSite} />
      <Route path="/site/reports" component={ReportsSite} />
      <Route path="/site/contracts" component={ContractsSite} />
      <Route path="/site/teams" component={TeamsSite} />
      <Route path="/site/salary" component={SalarySite} />


      {/* Worker Routes */}
      <Route path="/worker/dashboard" component={DashboardWorker} />
      <Route path="/worker/attendance" component={AttendanceWorker} />
      <Route path="/worker/contracts" component={ContractsWorker} />
      <Route path="/worker/profile" component={ProfileWorker} />


      {/* Super Admin Routes */}
      <Route path="/super/dashboard" component={DashboardSuper} />
      <Route path="/super/tenants" component={TenantsSuper} />
      <Route path="/super/approvals" component={ApprovalsSuper} />
      <Route path="/super/billing" component={BillingSuper} />
      <Route path="/super/tax" component={TaxSuper} />
      <Route path="/super/settings" component={SettingsSuper} />

      
      {/* Placeholder Routes for Next Phase */}
      <Route path="/login/social">
        {() => {
          // Mock Social Login Redirect
          setTimeout(() => window.location.href = "/role-select", 1000);
          return <div className="flex items-center justify-center h-screen">소셜 로그인 처리 중...</div>;
        }}
      </Route>

      {/* Fallback */}
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider defaultTheme="light">
        <TooltipProvider>
          <Toaster />
          <Router />
        </TooltipProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}

export default App;
