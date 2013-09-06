package androidclient.main;

import tamu.whoop.coursesketch.androidclient.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class CourseSketchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_sketch);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.course_sketch, menu);
		return true;
	}
}
