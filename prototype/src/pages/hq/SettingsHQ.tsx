import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Settings } from "lucide-react";

export default function SettingsHQ() {
  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        <h1 className="text-2xl font-bold">설정</h1>
        <Card>
          <CardHeader>
            <CardTitle>시스템 설정</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center py-12 text-muted-foreground">
              설정 항목이 여기에 표시됩니다.
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}



